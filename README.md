# SwipeOptionLayout
Open source Android Kotlin option side sliding option menu library with iOS like behaviour.

# Example usage

Close behaviour|Options usage
---|---

# Feature

- 1. Two-way sliding
- 2. Two side options
- 3. Support any View
- 4. iOS Like behaviour

```
implementation 'com.github.vcoolish:SwipeOptionLayout:1.0.0'
```

### Example layout

            <com.vcoolish.swipeoptionlayout.SwipeOptionLayout
                xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:id="@+id/swipeLayout"
                android:layout_width="match_parent"
                android:layout_height="72dp"
                app:contentView="@+id/content"
                app:leftMenuView="@+id/left"
                app:rightMenuView="@+id/right"
                app:leftMenuButton="@id/leftButton"
                app:rightMenuButton="@id/rightButton"
                app:canLeftSwipe="false"
                >
                <FrameLayout
                    android:id="@+id/left"
                    android:background="@android:color/holo_blue_dark"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    >
                    <TextView
                        android:id="@+id/leftButton"
                        android:layout_gravity="end|center_vertical"
                        android:gravity="center_vertical"
                        android:text="Left"
                        android:padding="20dp"
                        android:layout_width="wrap_content"
                        android:layout_height="match_parent"
                        />
                </FrameLayout>
                <FrameLayout
                    android:id="@+id/right"
                    android:background="@android:color/holo_red_light"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    >
                    <TextView
                        android:id="@+id/rightButton"
                        android:layout_gravity="center_vertical"
                        android:gravity="center_vertical"
                        android:padding="20dp"
                        android:text="Right"
                        android:layout_width="wrap_content"
                        android:layout_height="match_parent"
                        />
                </FrameLayout>
                <FrameLayout
                    android:id="@+id/content"
                    android:background="#cccccc"
                    android:orientation="vertical"
                    android:padding="20dp"
                    android:layout_width="match_parent"
                    android:layout_height="match_parent"
                    >
                    <TextView
                        android:id="@+id/contentTv"
                        android:gravity="center_vertical"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        />
                </FrameLayout>
            </com.vcoolish.swipeoptionlayout.SwipeOptionLayout>

# License

Trust Wallet Core is available under the MIT license. See the [LICENSE](LICENSE) file for more info.