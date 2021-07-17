const app = getApp();

Page({
  data: {
    ocrResult: null,
    imgPath: '/assets/demo-img.png',
  },
  localData: {
    imgType: 'unknown',
  },

  onLoad() {
    app.globalData.ocrResult.imgPath = this.data.imgPath;
  },

  onPullDownRefresh() {
    wx.reLaunch({url: '/pages/index/index'});
    wx.stopPullDownRefresh();
  },

  /**
   * 选择图片
   */
  selectImg() {
    console.log('选择图片');

    const selectModes = ['拍摄', '从相册选择', '从聊天选择'];
    const that = this;

    console.log('选择图片选择方式');
    wx.showActionSheet({
      itemList: selectModes,
      success(res) {
        console.log(`使用 "${selectModes[res.tapIndex]}" 方式获取图片`);

        // 拍摄
        if (res.tapIndex === 0) {
          wx.chooseImage({
            count: 1,
            sourceType: ['camera'],
            success: that.handleSelectImgSuccess,
            fail: that.handleSelectImgFail,
          });
        }

        // 从相册选择
        else if (res.tapIndex === 1) {
          wx.chooseImage({
            count: 1,
            sourceType: ['album'],
            success: that.handleSelectImgSuccess,
            fail: that.handleSelectImgFail,
          });
        }

        // 从聊天选择
        else {
          wx.chooseMessageFile({
            count: 1,
            type: 'image',
            success: that.handleSelectImgSuccess,
            fail: that.handleSelectImgFail,
          });
        }

      },
      fail() {
        console.log('放弃选择图片选择方式');
      }
    });
  },

  /**
   * 处理选择图片成功
   * 
   * @param res 图片选择结果，必须包含 .tempFiles 属性
   */
  handleSelectImgSuccess(res) {
    const path = res.tempFiles[0].path;
    const size = res.tempFiles[0].size;

    console.log(`选择图片成功，图片路径："${path}"，图片大小：${size}B`);

    // 检查图片大小
    // 图片不能大于 2.25MB ，这是 OCR 服务的限制
    if (size > 2359296) {
      console.log('图片大于 2.25MB ，无法上传');

      wx.showModal({
        title: '图片过大',
        content: '图片大小不能大于 2.25MB',
        showCancel: false,
      });

      return;
    }

    this.setData({
      imgPath: path,
    });

  },

  /**
   * 处理选择图片失败
   */
  handleSelectImgFail() {
    console.log('放弃选择图片');
  },


  /**
   * 上传图片
   */
  uploadImg() {
    const pathSlice = this.data.imgPath.split('.');
    const suffix = pathSlice[pathSlice.length-1].toLowerCase();
    this.localData.imgType = 'unknown';

    // 判断图片类型
    if (['jpg', 'jpeg'].indexOf(suffix) !== -1) {
      this.localData.imgType = 'image/jpeg';
      console.log('JPEG 格式的图片');
    }
    else if (suffix === 'png') {
      this.localData.imgType = 'image/png';
      console.log('PNG 格式的图片');
    }
    else {
      console.log('不支持的图像格式');

      wx.showModal({
        title: '不支持的图片格式',
        content: '目前仅支持 PNG 和 JPEG 格式的图片',
        showCancel: false,
      });

      return;
    }

    // 开始加载动画
    wx.showLoading({
      title: '正在读取图片文件...',
      mask: true,
    });

    // 读取图片内容
    console.log('正在读取图片文件...');
    const fileSystemManager = wx.getFileSystemManager();
    fileSystemManager.readFile({
      filePath: this.data.imgPath,
      encoding: 'base64',

      success: this.handleReadImgSuccess,

      fail(res) {
        console.log('读取文件失败，错误信息：', res.errMsg);
        wx.hideLoading();  // 结束加载动画
        wx.showToast({
          title: '图片文件读取失败',
          icon: 'none',
        });
      },
    });
  },

  /**
   * 处理图片读取成功
   * 
   * 作为 FileSystemManager.readFile 成功的回调
   * 图片读取成功后，将其内容上传到服务端
   * 
   * @param res 文件内容
   */
  handleReadImgSuccess(res) {
    console.log('文件读取成功，正在发起网络请求...');

    // 结束加载动画
    wx.hideLoading();

    // 开始加载动画
    wx.showLoading({
      title: '上传图片中...',
      mask: true,
    });

    wx.request({
      url: `${app.globalData.urls.baseUrl}${app.globalData.urls.uploadImgUrl}`,
      method: 'POST',
      data: {
        type: this.localData.imgType,
        image: res.data,
      },

      success: this.handleUploadImgSuccess,

      fail(res) {
        console.log('上传图片失败，错误信息：', res.errMsg);
        wx.hideLoading();  // 结束加载动画
        wx.showToast({
          title: '图片上传失败，请稍后再试',
          icon: 'none',
        });
      },
    });
  },


  /**
   * 处理图片上传成功
   * 
   * 作为 wx.request 成功的回调
   * 
   * @param res 接口返回信息
   */
  handleUploadImgSuccess(res) {

    if (res.statusCode !== 200) {
      console.log('上传图片时，服务端返回错误，错误信息：', res.data);
      wx.showToast({
        title: '图片上传失败，请稍后再试',
        icon: 'none',
      });
      return;
    }

    console.log(`图片上传成功，图片 id : '${res.data.id}'`);

    // 将结果 url 和图片路径记录到全局变量
    app.globalData.ocrResult.resultUrl = res.data.result;
    app.globalData.ocrResult.imgPath = this.data.imgPath;

    // 在历史记录中添加一条记录
    try {
      // 获取历史记录
      let histories = wx.getStorageSync('histories');

      if (!histories) {
        histories = [];
      }

      // 插入记录
      histories.splice(0, 0, {
        imgUrl: res.data.img_direct_url,
        resultUrl: res.data.result_direct_url
      });
      histories = histories.splice(0, app.globalData.maxHistoriesCount);

      // 保存历史记录
      wx.setStorageSync('histories', histories);
    } catch (e) {
      console.log('操作历史记录缓存失败，错误信息：', e);
    }

    // 结束加载动画
    wx.hideLoading();

    // 转到结果页
    wx.navigateTo({url: '/pages/result/result'});
  },


  /**
   * 跳转到历史记录页
   */
  handleSwitchToHistory() {
    wx.navigateTo({ url: '/pages/history/history' });
  },

});
