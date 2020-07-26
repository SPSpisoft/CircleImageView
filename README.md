# CircleImageView

CircleImageView是继承ImageView的类，能够显示圆形和圆角矩形的ImageView，还可以设置边框大小颜色，设置ImageView的宽高比例。

![图片](https://github.com/dingjianlun/CircleImageView/1.png)

### 集成
添加依赖
```gradle
implementation 'com.dingjianlun:circleimageview:0.0.1'
```

### 添加标签
```xml
<com.dingjianlun.circleimageview.CircleImageView

    //显示类型:圆角或者圆形
    app:civ_type="round"

    //边框颜色
    app:civ_border_color="#f00"
    //边框大小
    app:civ_border_width="4dp"

    //默认圆角
    app:civ_corner_radius="64dp"

    //圆角大小(覆盖默认圆角)
    app:civ_left_bottom_radius="32dp"
    app:civ_left_top_radius="8dp"
    app:civ_right_bottom_radius="24dp"
    app:civ_right_top_radius="16dp"

    //宽高比例
    app:civ_ratio_width="4"
    app:civ_ratio_height="3"

/>
```