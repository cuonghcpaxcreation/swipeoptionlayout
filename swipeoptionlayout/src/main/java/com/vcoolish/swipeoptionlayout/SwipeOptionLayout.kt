package com.vcoolish.swipeoptionlayout

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Scroller
import com.vcoolish.swipeoptionlayout.R
import kotlin.math.abs

class SwipeOptionLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ViewGroup(context, attrs, defStyleAttr) {

    private val mMatchParentChildren = ArrayList<View>(1)

    private var leftViewResID = 0
    private var rightViewResID = 0
    private var rightButtonResID = 0
    private var leftButtonResID = 0
    private var contentViewResID = 0

    private var leftView: View? = null
    private var leftButton: View? = null
    private var rightView: View? = null
    private var rightButton: View? = null
    private var contentView: View? = null

    private var contentViewLp: MarginLayoutParams? = null
    private var isSwiping = false
    private var lastP: PointF? = null
    private var firstP: PointF? = null

    var isCanLeftSwipe = true
    var isCanRightSwipe = true
    private var rightCallback: SwipeCallback? = null
    private var leftCallback: SwipeCallback? = null
    private var scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private var scroller: Scroller = Scroller(context)
    private var finalyDistanceX = 0f
    private var result: State? = null

    init {
        val typedArray = context.theme.obtainStyledAttributes(attrs, R.styleable.SwipeOptionLayout, defStyleAttr, 0)
        try {
            for (i in 0 until typedArray.indexCount) {
                when (typedArray.getIndex(i)) {
                    R.styleable.SwipeOptionLayout_leftMenuView -> leftViewResID = typedArray.getResourceId(R.styleable.SwipeOptionLayout_leftMenuView, -1)
                    R.styleable.SwipeOptionLayout_rightMenuView -> rightViewResID = typedArray.getResourceId(R.styleable.SwipeOptionLayout_rightMenuView, -1)
                    R.styleable.SwipeOptionLayout_leftMenuButton -> leftButtonResID = typedArray.getResourceId(R.styleable.SwipeOptionLayout_leftMenuButton, -1)
                    R.styleable.SwipeOptionLayout_rightMenuButton -> rightButtonResID = typedArray.getResourceId(R.styleable.SwipeOptionLayout_rightMenuButton, -1)
                    R.styleable.SwipeOptionLayout_contentView -> contentViewResID = typedArray.getResourceId(R.styleable.SwipeOptionLayout_contentView, -1)
                    R.styleable.SwipeOptionLayout_canLeftSwipe -> isCanLeftSwipe = typedArray.getBoolean(R.styleable.SwipeOptionLayout_canLeftSwipe, true)
                    R.styleable.SwipeOptionLayout_canRightSwipe -> isCanRightSwipe = typedArray.getBoolean(R.styleable.SwipeOptionLayout_canRightSwipe, true)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        isClickable = true
        var count = childCount
        val measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY
        mMatchParentChildren.clear()
        var maxHeight = 0
        var maxWidth = 0
        var childState = 0
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != View.GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0)
                val lp = child.layoutParams as MarginLayoutParams
                maxWidth = maxWidth.coerceAtLeast(child.measuredWidth + lp.leftMargin + lp.rightMargin)
                maxHeight = maxHeight.coerceAtLeast(child.measuredHeight + lp.topMargin + lp.bottomMargin)
                childState = View.combineMeasuredStates(childState, child.measuredState)
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT || lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child)
                    }
                }
            }
        }
        maxHeight = maxHeight.coerceAtLeast(suggestedMinimumHeight)
        maxWidth = maxWidth.coerceAtLeast(suggestedMinimumWidth)
        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState), View.resolveSizeAndState(maxHeight, heightMeasureSpec, childState shl View.MEASURED_HEIGHT_STATE_SHIFT))
        count = mMatchParentChildren.size
        if (count > 1) {
            for (i in 0 until count) {
                val child = mMatchParentChildren[i]
                val lp = child.layoutParams as MarginLayoutParams
                val childWidthMeasureSpec: Int
                childWidthMeasureSpec = if (lp.width == LayoutParams.MATCH_PARENT) {
                    val width = 0.coerceAtMost(measuredWidth - lp.leftMargin - lp.rightMargin)
                    MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY)
                } else {
                    getChildMeasureSpec(widthMeasureSpec,
                            lp.leftMargin + lp.rightMargin,
                            lp.width)
                }
                val childHeightMeasureSpec = if (lp.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                    val height = 0.coerceAtMost(measuredHeight - lp.topMargin - lp.bottomMargin)
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
                } else {
                    getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height)
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            }
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val left = 0 + paddingLeft
        val top = 0 + paddingTop
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (leftView == null && child.id == leftViewResID) {
                leftView = child
                val button = (child as? ViewGroup)?.getChildAt(0)
                if (leftButton == null && button?.id == leftButtonResID) {
                    leftButton = button
                }
            } else if (rightView == null && child.id == rightViewResID) {
                rightView = child
                val button = (child as? ViewGroup)?.getChildAt(0)
                if (rightButton == null && button?.id == rightButtonResID) {
                    rightButton = button
                }
            } else if (contentView == null && child.id == contentViewResID) {
                contentView = child
            }
        }
        contentView?.let { mContentView ->
            contentViewLp = (mContentView.layoutParams as? MarginLayoutParams)?.also { mContentViewLp ->
                val cTop = top + mContentViewLp.topMargin
                val cLeft = left + mContentViewLp.leftMargin
                val cRight = left + mContentViewLp.leftMargin + mContentView.measuredWidth
                val cBottom = cTop + mContentView.measuredHeight
                mContentView.layout(cLeft, cTop, cRight, cBottom)
                leftView?.let { mLeftView ->
                    (mLeftView.layoutParams as? MarginLayoutParams)?.let { leftViewLp ->
                        val lTop = top + leftViewLp.topMargin
                        val lLeft = 0 - mLeftView.measuredWidth + leftViewLp.leftMargin + leftViewLp.rightMargin
                        val lRight = 0 - leftViewLp.rightMargin
                        val lBottom = lTop + mLeftView.measuredHeight
                        mLeftView.layout(lLeft, lTop, lRight, lBottom)
                    }
                }
                rightView?.let { mRightView ->
                    (mRightView.layoutParams as? MarginLayoutParams)?.let { rightViewLp ->
                        val lTop = top + rightViewLp.topMargin
                        val lLeft = mContentView.right + mContentViewLp.rightMargin + rightViewLp.leftMargin
                        val lRight = lLeft + mRightView.measuredWidth
                        val lBottom = lTop + mRightView.measuredHeight
                        mRightView.layout(lLeft, lTop, lRight, lBottom)
                    }
                }
            }
        }
        rightButton?.setOnClickListener {
            rightCallback?.onSlide(this)
            handlerSwipeMenu(State.CLOSE)
        }
        leftButton?.setOnClickListener {
            leftCallback?.onSlide(this)
            handlerSwipeMenu(State.CLOSE)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        run {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    isSwiping = false
                    if (lastP == null) {
                        lastP = PointF()
                    }
                    lastP!![ev.rawX] = ev.rawY
                    if (firstP == null) {
                        firstP = PointF()
                    }
                    firstP!![ev.rawX] = ev.rawY
                    if (viewCache != null) {
                        if (viewCache !== this) {
                            viewCache!!.handlerSwipeMenu(State.CLOSE)
                        }
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val distanceX = lastP!!.x - ev.rawX
                    val distanceY = lastP!!.y - ev.rawY
                    if (abs(distanceY) > scaledTouchSlop && abs(distanceY) > abs(distanceX)) {
                        return@run
                    }
                    scrollBy(distanceX.toInt(), 0)
                    if (scrollX < 0) {
                        if (!isCanRightSwipe || leftView == null) {
                            scrollTo(0, 0)
                        } else {
                            if (scrollX < leftView!!.left) {
                                scrollTo(leftView!!.left, 0)
                            }
                        }
                    } else if (scrollX > 0) {
                        if (!isCanLeftSwipe || rightView == null) {
                            scrollTo(0, 0)
                        } else {
                            if (scrollX > rightView!!.right - contentView!!.right - contentViewLp!!.rightMargin) {
                                scrollTo(rightView!!.right - contentView!!.right - contentViewLp!!.rightMargin, 0)
                            }
                        }
                    }
                    if (abs(distanceX) > scaledTouchSlop) {
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    lastP!![ev.rawX] = ev.rawY
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    finalyDistanceX = (firstP?.x ?: 0f) - ev.rawX
                    if (abs(finalyDistanceX) > scaledTouchSlop) {
                        isSwiping = true
                    }
                    result = isShouldOpen()
                    handlerSwipeMenu(result)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> if (abs(finalyDistanceX) > scaledTouchSlop) {
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> if (isSwiping) {
                isSwiping = false
                finalyDistanceX = 0f
                return true
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    private fun handlerSwipeMenu(result: State?) {
        when {
            result == State.LEFTOPEN && leftButton != null -> {
                scroller.startScroll(scrollX, 0, -leftButton!!.width - scrollX, 0)
                viewCache = this
                stateCache = result
            }
            result == State.RIGHTOPEN && rightButton != null -> {
                viewCache = this
                scroller.startScroll(scrollX, 0, rightButton!!.width - scrollX, 0)
                stateCache = result
            }
            result == State.LEFTSLIDE && leftView != null -> {
                scroller.startScroll(scrollX, 0, -leftView!!.width - scrollX, 0)
                viewCache = this
                stateCache = result
                leftCallback?.onSlide(this)
                handlerSwipeMenu(State.CLOSE)
            }
            result == State.RIGHTSLIDE && contentView != null -> {
                viewCache = this
                scroller.startScroll(scrollX, 0, contentView!!.width - scrollX, 0)
                stateCache = result
                rightCallback?.onSlide(this)
                handlerSwipeMenu(State.CLOSE)
            }
            else -> {
                scroller.startScroll(scrollX, 0, -scrollX, 0)
                viewCache = null
                stateCache = null
            }
        }
        invalidate()
    }

    override fun computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.currX, scroller.currY)
            invalidate()
        }
    }

    private fun isShouldOpen(): State? {
        if (scaledTouchSlop >= abs(finalyDistanceX)) {
            return stateCache
        }
        if (finalyDistanceX < 0) {
            if (scrollX < 0 && leftView != null) {
                if (leftView!!.width * 0.5 < abs(scrollX)) {
                    return State.LEFTSLIDE
                } else if (leftView!!.width * 0.1 < abs(scrollX)) {
                    return State.LEFTOPEN
                }
            }
            if (scrollX > 0 && rightView != null) {
                return State.CLOSE
            }
        } else if (finalyDistanceX > 0) {
            if (scrollX > 0 && rightView != null) {
                if (abs(rightView!!.width * 0.5) < abs(scrollX)) {
                    return State.RIGHTSLIDE
                } else if (abs(rightView!!.width * 0.1) < abs(scrollX)) {
                    return State.RIGHTOPEN
                }
            }
            if (scrollX < 0 && leftView != null) {
                return State.CLOSE
            }
        }
        return State.CLOSE
    }

    override fun onDetachedFromWindow() {
        if (this == viewCache) {
            viewCache?.handlerSwipeMenu(State.CLOSE)
        }
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (this == viewCache) {
            viewCache?.handlerSwipeMenu(stateCache)
        }
    }

    fun setRightSwipeCallback(swipeCallback: SwipeCallback?) {
        rightCallback = swipeCallback
    }

    fun setLeftSwipeCallback(swipeCallback: SwipeCallback?) {
        leftCallback = swipeCallback
    }

    companion object {
        var viewCache: SwipeOptionLayout? = null
            private set
        var stateCache: State? = null
            private set
    }

    interface SwipeCallback {
        fun onSlide(view: View)
    }

    enum class State {
        LEFTOPEN,
        RIGHTOPEN,
        LEFTSLIDE,
        RIGHTSLIDE,
        CLOSE
    }
}