# 俄罗斯方块

这个项目是我大一下学期期末时写的“程序设计基础实训”的大作业，一个用C实现的Windows命令行程序。

我对Tetris的兴趣就是从写这个项目开始的，最近由于对Tetris的兴趣越发难以控制，所以特别整理了一下这个项目...

---

俄罗斯方块（Tetris®）是一款令人着迷的益智类游戏。这款游戏最初是由阿列克谢·帕基特诺夫于1984年发明的。

简单而优雅的设计除了使它成为一款风靡全球的游戏，也使它深受软件开发人员的喜爱，而常作为软硬件的测试程序。其在程序员心中的地位甚至不低于著名的"Hello World!"程序，编写Tetris®和"Hello World!"是每个程序员的必备技能，

> 现在，我已经会编写"Hello World!"了，所以我该试着写Tetris®

## 如何开始

迫不及待想试着玩一下了吗，你只需要双击项目文件夹中的`Tetris.exe`文件即可开始，只需参照游戏内的键位及快捷命令提示即可体验这款游戏精致的操控。

一般来说你不需要关注项目文件夹内的其他文件，但我还是很乐意向你介绍他们。因为既然你都看了这篇文档，所以你也许真的很有可能想知道那些起着奇怪名字的文件都是些什么。

```
.
├─ Tetris.c
├─ Tetris.exe
├─ Tetris.md
├─ controls_and_scoring.png
└─ README.md
```

就上面这些了，不能再多了。

`Tetris.c` 该游戏的源代码，请在Windows系统下编译，UTF-8编码；

`Tetris.exe` 编译的该游戏，可在Windows系统下直接运行；

`Tetris.md` 对于该游戏的官方说明；

`README.md` 你正在看的这篇文档，不能再无聊了。

你不会再需要从我这里知道更多了，不要问我`controls_and_scoring.png`是什么，如果你真的有这么大的好奇，你一定能自己找到答案。

以上文件，包括源代码，你可以完全按照你的想法去使用他们，你可以不必署上我的名字（你甚至也可以不用记得每一行代码都是我自己敲出来的），甚至你可以署上你的名字然后买一个好价钱。当然，如果你想把收入的一部分给我作为[*打赏*](https://keyboard-l.github.io/myBlog/donate/)我也是很乐意的，如果是这样，你只需要点一下那个链接。我没有为该项目专门制作打赏页面，你看到的是我的个人博客的一部分，如果你愿意你也可以顺便参观一下我的个人博客。里面也有一些不错的东西。

## 向原作致敬

从Tetris®诞生至今，这款游戏产生了不计其数的派生版本。但无可否认的是，所有的这些花哨的版本都远不能与原版的优雅媲美。我无意重新发明轮子，也远没有超越原作的远大抱负，因此我选择带着虔诚的心，穷尽我的能力，尽力去实现原版Tetris®的体验，以向原作及作者致敬。

为了能尽可能地还原最地道的Tetris®，我花了相当长的时间去体验我能找到的最正统的Tetris®。如果你也想尝试一下，我很乐意分享这个链接，[TETEIS®](https://tetris.com/play-tetris/).

> 如果你对你的技术很有信心，你一定很想了解到我最高拿了351792分，并轻易打败我。

而往下看你会知道，我的程序的不断迭代的过程也同时是不断去玩这个游戏的过程，在我看来，玩这个游戏和写这个游戏是同等的（尽管只有其中一个可以为我赢得学分）。

## 历史版本更新

2017.06.09

实现移动、旋转等基本功能

实现基本游戏流程

加入记分系统、加入显示下一方块

2017.06.10

重构代码，加入Rotate Right、Rotate Left、Hard Drop、Soft Drop、Hold、Pause功能，增加显示接下来三个方块

2017.06.11

重新设计外观，使之更接近官方版

完善逻辑

按照官方版本，重新设计计分系统

2017.06.12

更多的测试，调整速度及难度变化幅度

写了这篇无聊的文档

## 一些已知的Issue

1. 不要给我抱怨屏幕闪烁，那是我故意加上去的，为了考验你的眼睛；
2. 当点击窗口内的某些位置时，可能会导致因字符宽度问题而引起的显示错位；
3. 在某些情况下右边栏的右下角的部分制表符显示不全；
4. LEVEL不能高于8，因为如果是那样，你几乎在一瞬间就GAME OVER了，从LEVEL 7到LEVEL 8的速度变化是突变的；（如果你看了源代码，你肯定能知道这是为什么）
5. 你可以使用HPKM代替↑↓←→键执行控制，如果想知道为什么，你一定要看看源代码；
6. 在方块落到底部时你将不能像官方版本一样通过不停地移动和旋转延迟方块的固定；
7. T-Spin并不能为你加分，而且大多数T-Spin和Spin操作都无法成功，因为我没有设置备用的旋转中心，也没有在记分系统中考虑这个因素。如果你因此质疑我的能力，那你一定是过于狂妄了，我不过是认为你不可能做出一个Spin或者T-Spin。
8. 当你的得分达到1亿或以上时，你左边栏的分数显示会有问题，但那不代表你的分数有问题，在游戏结束后你可以看到正确的得分。但是如果你的得分超过1千亿，那我也帮不了你了。所以，为了你发朋友圈顺利，你一定不要玩到那么高分。
9. 最诡异的一点，当你在24时左右玩这个游戏时，你会发现游戏有可能进行得异常缓慢，除非你达到LEVEL 8。当然，这不代表你可以借此达到一亿分。就算你玩到屏幕闪得什么都看不见你也不可能达到一亿分。

## 更多的了解

想对Tetris®有更多的了解，可以访问[官方网站](https://tetris.com/)，或查看文档[Tetris.md](Tetris.md)。

### 关于作者

详情见[Keyboard的个人博客](https://blog.keybrl.com/)