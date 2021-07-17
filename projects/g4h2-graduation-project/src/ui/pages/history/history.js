const app = getApp();

Page({

  /**
   * 页面的初始数据
   */
  data: {
    histories: []
  },
  localData: {
    historyLongtapFlag: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad: function (options) {
    this.loadHistories();
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh: function () {
    this.loadHistories();
    wx.stopPullDownRefresh();
  },


  /**
   * 处理切换到某历史记录详情页
   * 
   * @event 点击事件
   */
  handleSwitchToResult(event) {
    if (this.localData.historyLongtapFlag) {
      this.localData.historyLongtapFlag = false;
      return;
    }

    const i = parseInt(event.currentTarget.id);
    app.globalData.ocrResult.imgPath = this.data.histories[i].imgUrl;
    app.globalData.ocrResult.resultUrl = this.data.histories[i].resultUrl;
    wx.navigateTo({ url: '/pages/result/result' });
  },

  /**
   * 处理删除某条历史记录
   * 
   * @event 长按事件
   */
  handleDelHistory(event) {
    const that = this;

    this.localData.historyLongtapFlag = true;

    const i = parseInt(event.currentTarget.id);

    wx.showModal({
      title: '删除记录',
      content: '是否删除该条历史记录？',
      success(res) {
        if (res.confirm) {
          console.log(`确认删除记录：${i}`);

          that.data.histories.splice(i, 1);
          that.setData({
            histories: that.data.histories
          });

          try {
            that.saveHistories();
            wx.showToast({
              title: '删除成功',
              mask: true
            });
          } catch (e) {
            console.log('保存历史记录失败，错误信息：', e);
            wx.showToast({
              title: '删除失败',
              icon: none,
              mask: true
            });
            that.loadHistories();
          }
        } else {
          console.log(`取消删除记录：${i}`);
        }
      },
      fail(res) {
        console.log('确认删除对话框弹出失败，错误信息：', res.errMsg);
      }
    })
  },


  /**
   * 加载历史记录
   */
  loadHistories() {
    try {
      const histories = wx.getStorageSync('histories');

      console.log('历史记录：', histories);

      this.setData({
        histories: histories
      });
    } catch (e) {
      console.log('获取历史记录缓存时发生错误，错误信息：', e);
    }
  },

  /**
   * 保存历史记录
   */
  saveHistories() {
    wx.setStorageSync('histories', this.data.histories);
  },

})
