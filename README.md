# KRefreshLayout  
继承自FrameLayout的下拉和上拉刷新     
![](https://github.com/CharryLi/KRefreshLayout/blob/master/gif/pullrefresh01.gif?raw=true)  
![](https://github.com/CharryLi/KRefreshLayout/blob/master/gif/loadmore01.gif?raw=true)

##如何使用
Gradle  
`compile 'com.charry.krefresh:krefresh:0.1.1'`
  
  
XML中   
```xml
<com.charry.krefresh.KRefreshLayout
        android:id="@+id/refreshlayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <ListView
            android:id="@+id/listview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@android:color/white"/>
</com.charry.krefresh.KRefreshLayout>
```  
  
代码中  
```java  
KRefreshLayout mRefreshLayout = (KRefreshLayout) findViewById(R.id.refreshlayout);
mRefreshLayout.setOnRefreshListener(new KRefreshLayout.KOnRefreshListener() {
    @Override
    public void onRefresh() {
        Toast.makeText(MainActivity.this, "刷新回调被调用了", Toast.LENGTH_SHORT).show();
    }
});
mRefreshLayout.setOnLoadMoreListener(new KRefreshLayout.KOnLoadMoreListener() {
    @Override
    public void onLoadMore() {
        Toast.makeText(MainActivity.this, "上拉回调被调用了", Toast.LENGTH_SHORT).show();
    }
});
```  
  
自动刷新  
```java    
mRefreshLayout.startRefreshWithCallBack();// 自动回调onRefresh
mRefreshLayout.startRefreshWithCallBack(100);// 延迟100毫秒刷新，自动回调onRefresh  
```  
  
结束刷新  
```java  
mRefreshLayout.finishRefresh();  
```  
  
结束加载更多  
```java  
mRefreshLayout.finishLoadMore();
```  
  
其他  
```java  
mRefreshLayout.setRefreshing(true);// 进入刷新状态，但不回调onRefresh  
mRefreshLayout.setRefreshing(false);// 结束刷新
```  
  
##自定义刷新头视图  
1. 新建类继承自`KBaseRefreshHead`，重写`pullAction`、`releaseRefreshAction`、`refreshAction`、`refreshCompleteAction`在相对应的回调方法中编写自己的头部动画；
2. 在activity中`mRefreshLayout.setCustomHeadView(new CustomHeadView(this));`设置自定义头，如果需要自定义头不是图高度，需要在`setCustomHeadView`方法前调用`setHeadViewHeight`;