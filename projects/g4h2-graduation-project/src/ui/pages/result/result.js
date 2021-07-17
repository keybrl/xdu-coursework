const app = getApp();

Page({
  data: {
    imgPath: null,
    summaryResult: null,
    detailedResult: null,
  },

  onLoad: function (options) {
    if (!app.globalData.ocrResult.imgPath || !app.globalData.ocrResult.resultUrl) {
      console.log('图片还未上传，没有结果，返回首页')
      wx.reLaunch({url: '/pages/index/index'})
      return;
    }

    this.getOcrResult(app.globalData.ocrResult.resultUrl);

  },

  onUnload() {
    console.log('离开结果页，清理残留状态');
    app.globalData.ocrResult.imgPath = null;
    app.globalData.ocrResult.resultUrl = null;
  },

  onPullDownRefresh() {
    this.getOcrResult(app.globalData.ocrResult.resultUrl);
    wx.stopPullDownRefresh();
  },


  /**
   * 查询图片 OCR 结果
   * 
   * @param resultUrl 能够获取结果的 url
   */
  getOcrResult(resultUrl) {
    console.log('正在查询 OCR 结果...');

    // 开始加载动画
    wx.showLoading({
      title: '图像识别中...',
      mask: true,
    });

    wx.request({
      url: resultUrl,
      success: this.handleGetOcrResultSuccess,
      fail(res) {
        console.log('查询 OCR 结果失败，错误信息：', res.errMsg);
        wx.hideLoading();  // 结束加载动画
        wx.showToast({
          title: '查询识别结果失败，请稍后再试',
          icon: 'none',
        });
      }
    });
  },

  /**
   * 处理查询结果成功
   * 
   * 作为 wx.request 成功的回调
   * 
   * @param res 接口返回信息
   */
  handleGetOcrResultSuccess(res) {
    if (res.statusCode !== 200) {
      console.log('查询结果时，服务端返回错误，错误信息：', res.data);
      wx.showToast({
        title: '查询识别结果失败，请稍后再试',
        icon: 'none',
      });
      return;
    }

    console.log('OCR 结果查询成功，结果：', res.data);

    let result = this.spliceResult(res.data);

    console.log(`识别结果：\n${result}`);

    this.setData({
      imgPath: app.globalData.ocrResult.imgPath,
      summaryResult: result,
      detailedResult: res.data,
    });
    this.drawTextArea(res.data);

    // 结束加载动画
    wx.hideLoading();
  },


  /**
   * 返回主页
   */
  handleBackHome() {
    wx.reLaunch({
      url: '/pages/index/index',
    });
  },

  /**
   * 拷贝结果到剪贴板
   */
  handleCopyResult() {
    wx.setClipboardData({
      data: this.data.summaryResult,
      fail(res) {
        console.log(res);
        wx.showToast({
          title: '无法复制到剪贴板，请手动选择/复制',
          icon: 'none',
        });
      },
    });
  },


  /**
   * 绘出文字检测结果
   *
   * @param data 文字识别结果的 `data` 属性
   */
  drawTextArea(data) {
    const imgElemId = 'result-image';
    const canvasId = 'resultMarks';
    const imgPath = app.globalData.ocrResult.imgPath;
    let imgSize = {width: -1, height: -1};
    let imgViewSize = {width: -1, height: -1};
    let scale = -1;
    let offset = {x: 0, y: 0};

    const draw = function() {
      const ctx = wx.createCanvasContext(canvasId);
      ctx.scale(scale, scale);
      ctx.setLineWidth(1 / scale);
      ctx.setStrokeStyle('#0000ff');

      for (let i = 0; i < data.length; i++) {
        let polygon = data[i].Polygon;
        if (polygon.length <= 2) {
          continue;
        }

        ctx.moveTo(polygon[0].X + offset.x, polygon[0].Y + offset.y);
        for (let j = polygon.length - 1; j >= 0; j--) {
          ctx.lineTo(polygon[j].X + offset.x, polygon[j].Y + offset.y);
        }
      }

      ctx.stroke();
      ctx.draw();

      console.log('绘图完成');
    };

    const calcScaleAndOffset = function() {
      if (imgSize.width === -1 || imgSize.height === -1) {
        console.log('未能获取图片实际宽高');
        return;
      }
      else if (imgViewSize.width === -1 || imgViewSize.height === -1) {
        console.log('未能获取图片显示宽高');
        return;
      }

      // 长宽各自的缩放比
      const scaleX = imgViewSize.width / imgSize.width;
      const scaleY = imgViewSize.height / imgSize.height;

      // 受制于宽度
      if (scaleX < scaleY) {
        scale = scaleX;
        offset.y = (imgViewSize.height - imgSize.height * scale) / 2 / scale;
      }

      // 受制于高度
      else {
        scale = scaleY;
        offset.x = (imgViewSize.width - imgSize.width * scale) / 2 / scale;
      }

      console.log(`画布缩放比： ${scale} ， X 轴偏移： ${offset.x} ， Y 轴偏移： ${offset.y}`);

      // 绘图
      draw();
    };

    const getImgViewSize = function() {
      const query = wx.createSelectorQuery();
      query.select(`#${imgElemId}`).boundingClientRect();
      query.exec(function (res) {
        imgViewSize.width = res[0].width;
        imgViewSize.height = res[0].height;
        console.log(`获取图片视图成功，图片显示宽高： ${imgViewSize.width}px * ${imgViewSize.height}px`);

        // 计算画布缩放和坐标偏移
        calcScaleAndOffset();
      });
    };

    const getImgSize = function() {
      wx.getImageInfo({
        src: imgPath,
        success(res) {
          imgSize.width = res.width;
          imgSize.height = res.height;
          console.log(`获取图片信息成功，图片原始宽高： ${imgSize.width}px * ${imgSize.height}px`);

          // 获取图片显示大小
          getImgViewSize();
        },
        fail(res) {
          console.log(`获取图片信息失败，错误信息${res.errMsg}`);
        },
      });
    };

    // 获取图片大小
    getImgSize();
  },


  /**
   * 拼合文字识别结果
   * 
   * @param data 文字识别结果的 `data` 属性
   */
  spliceResult(data) {
    let result = [];

    // 按段落、行组织文本
    for (let i = 0; i < data.length; i++) {
      let paragNo = JSON.parse(data[i].AdvancedInfo).Parag.ParagNo;

      if (result[paragNo - 1] === undefined) {
        result[paragNo - 1] = [];
      }

      result[paragNo - 1][result[paragNo - 1].length] = data[i].DetectedText;
    }

    // 拼合行
    for (let i = 0; i < result.length; i++) {
      result[i] = result[i].join('\n');
    }

    // 拼合段落
    result = result.join('\n\n');

    return result;
  }

});
