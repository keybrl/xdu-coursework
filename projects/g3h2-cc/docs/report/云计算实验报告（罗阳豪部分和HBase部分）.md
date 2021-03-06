# 云计算实验报告（罗阳豪部分和HBase部分）

> 其他要求：
>
> 1. 实验报告不少于10页（除封面）
> 2. 正文小四号（宋体正文），1.5倍行间距
> 3. 小组名称自拟
> 4. 每个小组只需要交一份大作业（电子版）、答辩ppt和代码（不要环境），验收完成后打包上交给课代表，由课代表统一上交
> 5. 特别注意：不要在网上拷贝一些Spark、HBase等组件的科普介绍信息放到报告中充字数

## 一、系统架构

> 描述系统整体架构，配上系统架构图，简单介绍系统

## 二、数据流程分析

### 1. 数据采集过程分析

> 分析数据采集到入库的流程，清楚描述每个软件模块在其中起到的作用，要求有流程图

#### HBase部分

Redis方面采用消息发布订阅的方式与其他客户端通信，所以HBase部分在此就使用如下流程进行数据搬运

![hbase_process_g](./image/hbase_process_g.png)

如图，一个守护进程运行，他会在Redis指定频道上监听来自其他Redis客户端的消息，如果得到转移数据的指示（Redis部分通常会在存满5000条数据时发送一次这种消息），就调起从Redis往HBase转移数据的进程。该流程如下

![process_of_t_redis2hbase](./image/process_of_t_redis2hbase.png)

使用spop方法，从Redis获取数据，即获取后该数据将从Redis删除，循环读取，直到Redis空则结束该过程。由于HBase开始和结束一次插入操作比较慢，所以，先把数据缓存在内存中，积累够一定数量后再插入到HBase中。

### 2. 数据查询和离线分析处理

> 离线处理指的是spark处理过程，要求自己写，不可以拷贝提供的实验文档

## 三、软件功能分析

### 1. 系统固有功能分析

> 介绍系统自带功能，即集群搭建完成后系统可提供的服务，要求每个服务都有独立的截图和详细介绍

#### Redis -> HBase

首先我们实现了从Redis获取数据并存入HBase中的方法。使用spop从Redis获取数据，这样Redis中该数据将会被删除，循环获取，缓存插入HBase，直到Redis中数据集为空。相关实现如下

![spop](./image/spop.jpg)

该过程可以定时执行，也可以配合Redis部分，使用消息发布订阅的方法。在消息发布订阅模式下，首先在一个守护进程中监听一个与Redis部分协商好的频道，等待接收特定消息。如果从该频道接收到特定消息（一般意味着Redis已经缓存了足够多的数据），则启动一次上述从Redis往HBase搬运数据的过程，结束后继续进入监听频道的状态。监听频道的相关实现如下

![p&s](./image/p&s.jpg)

数据插入HBase中，HBase中部分数据如下

![hbase_data](./image/hbase_data.jpg)

### 2. 系统附加功能分析

> 介绍小组添加的加分项功能，要求每个服务都有独立的截图和详细介绍

#### HBase - 车辆轨迹重现

车辆轨迹重现，要求是根据车辆eid查询给定时间范围内的轨迹（点坐标及其出现时间的集合）。

从原理上来说，只要重新编排行健，使用 eid##time##palceId 作为行健，由于HBase插入数据时。行健默认按字典序，所以对于待查 eid、start_time、end_time 只要对表进行行健从 eid##start_time##0 到 eid##end_time##999 范围执行 scan 操作即可获得结果。

在具体操作上，我们为了能够实时处理，而不需要离线计算，我们在插入车辆位置记录时就将数据存了两份。一份是按照原本的格式存在原本的表上（record），另一份是重新编排行健后，存在另一张表上（record2）。这样原本的业务逻辑可以在原来的表上进行，关于车辆轨迹的查询就在record2表上进行，这两张表就是同一数据的两种不同表示。插入数据的相关实现如下

![insert_record](./image/insert_record.jpg)

查询轨迹的相关实现如下

![get_places](./image/get_places.jpg)

为了方便前端的同学，我们使用Python的Flask框架，将该功能封装在一个Web接口中（独立于其他功能的后端接口）。该接口能够接收GET的查询参数、POST的表单、POST的JSON作为该过程的参数，返回JSON格式的数据。以下是一份返回给前端的数据示例

![json_data](./image/json_data.jpg)

## 四、实验感受及收获

> 小组内每名成员都要写，格式如下：  
> 张三：感受即收获（100字以上，希望不要写套话）

罗阳豪：

在这个项目中，我负责的是HBase部分。这并不是我第一次使用HBase，18年下半年我所修“数据管理技术”和“海量数据管理”课程都接触过HBase，这些经验使我在环境搭建上并没有走特别多的弯路，使我能够把更多的精力放在这个软件系统与HBase相关功能的实现上。

如果说之前的经验是关于如何搭建HBase集群，那么我现在的收获就是真正尝试了去使用HBase。在这个项目上，我没有使用更常见的Java来操作HBase，而是使用了Python。HBase的REST接口与Python的Requests（Python一个的第三方HTTP库）的优雅配合，Python丰富的数据结构和相对扁平的编程风格，使得我在应对这种业务逻辑复杂的数据库应用时十分轻松愉快。

HBase的逻辑和关系型数据库完全不同，初看非常有趣，设计思想也很特别。但是真正去试着实现一些功能，才能认识到其实列式存储的应用场景还是非常局限的。性能也是要分情况的，列式存储通常只在某些很简单的查询场景下，可以有非常不错的性能。这样看来倒还不如Redis有趣。

应用之下，集群的管理不是一件轻松的事情。在每一件浪费劳动力的事情上，我都希望通过编写可复用的bash脚本来使其尽量便捷，比如拷贝配置文件，繁杂的开关集群流程。但是麻烦仍然很多，每次网络环境变化，每次开关虚拟机之后，都有可能出现很多问题。所以我还学会了准备了一套备份数据、重置状态、重新正确运行的方法，并且在一次次重启中逐渐完善。这些经历让我明白，很多事情非常无聊而且浪费生命，而且避免被这些事困扰的方法也相当无聊而且浪费生命。

再有就是HBase算得上在整个软件系统中的中间位置，上承Web应用、下接Redis、侧通Spark，所以跟队友交互也是相当多。在这个过程中，如何与队友配合，如何划清与队友的工作的界限，这都很复杂。简单来说，自己了解相邻部分的工作总会使这个交互过程更顺畅。比如我也安装Redis，我也尝试搭建Spark集群，我没有写Web，但我本身就是熟悉Web开发的。这些看起来和我自己HBase这部分工作没什么关系，也不会体现在最终结果上，但我觉得对团队工作顺利进行还是很有意义的。

## 五、课程建议

> 对课程实施的一些建议，便于老师日后教学

（希望能把这段加到最终的课程建议中）

关于分布式，我觉得这个实验的要求与我的理解有一定偏差。难道一台主机上的多个虚拟机组成的集群就不是分布式吗？在某些应用场景下，对不同硬件资源的需求不太一样。比如HBase对CPU的需求相对低，而对内存、外存、IO需求相对高。这样一台主机只跑一个HBase节点是十分浪费的，因为CPU根本跑不满。虚拟化是所谓云计算应用的重要基础技术，虚拟化可以更好地均衡硬件资源配置。而且如今的虚拟化和半虚拟化技术已经可以使CPU性能损失忽略不计，通过vt-d技术划分直通PCI-E设备也可以使IO性能损失忽略不计，所以在考虑系统构建的时候，并不能简单地认为虚拟机就比真实机器差，应该基于具体的场景，选择合适的方案。就该项目所使用的HBase集群来说，一台电脑上的三个虚拟机性能远好于三台电脑上的三个虚拟机（实际上也远好于三台电脑上的9台虚拟机）。因为在这个应用场景下，第一个瓶颈是网络IO，在我们没有万兆位交换机，甚至连千兆位交换网都很难搭建的情况下（因为我们不是每台电脑都有以太网口），我们使用的无线局域网内TCP传输速率低于10Mbps（因为机器数目过多，网络设备较差，空间信道噪声较大等原因）。而我单台主机上虚拟机之间借助半虚拟化的网卡驱动可以轻易达到10Gbps的传输速率（也就是上一种情况的1000倍），HBase无论是查询响应速率、数据读写速率都远快于使用所谓“真”分布式的情况。所以我觉得这个课程不应该灌输类似“机器越多越好”这种定式思维，更不能鼓励大家做“吃力不讨好”的事情，而是应该要求大家根据组内具体的硬件配置情况，自己构建合适的分布式系统。
