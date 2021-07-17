App({
  onLaunch: function () {
  },
  globalData: {
    // 接口 URL
    urls: {
      baseUrl: 'https://ocr.keyboardluo.com/release/',
      uploadImgUrl: 'upload_img_to_cos'
    },

    // OCR 结果，作为结果页的入参
    ocrResult: {
      imgPath: null,
      resultUrl: null,
    },

    // 最大历史记录条数
    maxHistoriesCount: 200
  },
});
