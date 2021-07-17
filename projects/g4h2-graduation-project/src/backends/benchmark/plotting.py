import json
import numpy
from matplotlib import pyplot
from matplotlib.pyplot import MultipleLocator
from pylab import mpl


def single_thread_data(is_sort: bool = False):
    with open('res/single_thread_200.json', 'rt', encoding='utf-8') as fp:
        res = json.load(fp)

    if is_sort:
        res['res'].sort(key=lambda i: i.get('total_delay') if i.get('total_delay') else 0)

    y = [i['total_delay'] for i in res['res'] if i['success']]
    y1 = [i['get_res_delay'] for i in res['res'] if i['success']]
    y2 = [i['upload_delay'] for i in res['res'] if i['success']]
    x = range(1, len(y) + 1)

    mpl.rcParams['font.sans-serif'] = ['Microsoft YaHei']
    mpl.rcParams['axes.unicode_minus'] = False
    pyplot.figure(figsize=(16, 8))
    pyplot.ylabel('响应时间（s）')
    pyplot.xlim((0, 205))
    pyplot.ylim((0, 5))
    ax = pyplot.gca()
    x_major_locator = MultipleLocator(10)
    y_major_locator = MultipleLocator(0.5)
    ax.xaxis.set_major_locator(x_major_locator)
    ax.yaxis.set_major_locator(y_major_locator)

    if is_sort:
        pyplot.plot(x, y, '.k', label='总响应时间')
        pyplot.plot(x, y1, '_k', label='获取结果响应时间')
        pyplot.plot(x, y2, '+k', label='上传响应时间')

        pyplot.title('单线程连续请求响应时间（按总响应时间升序排列）')
        pyplot.xlabel('数据编号')
        pyplot.legend(loc='upper left')

        pyplot.savefig('res/single_thread_sorted_200.png')
    else:
        pyplot.plot(x, y, '-k', label='总响应时间')
        pyplot.plot(x, y1, '--k', label='获取结果响应时间')
        pyplot.plot(x, y2, ':k', label='上传响应时间')

        pyplot.title('单线程连续请求响应时间（按请求先后顺序排列）')
        pyplot.xlabel('数据序号')
        pyplot.legend(loc='upper left')

        pyplot.savefig('res/single_thread_200.png')

    pyplot.show()

    total_delay = (numpy.min(y), numpy.mean(y), numpy.median(y), numpy.max(y))
    get_res_delay = (numpy.min(y1), numpy.mean(y1), numpy.median(y1), numpy.max(y1))
    upload_delay = (numpy.min(y2), numpy.mean(y2), numpy.median(y2), numpy.max(y2))

    print(total_delay)
    print(get_res_delay)
    print(upload_delay)


def multi_thread_data(is_sort: bool):
    with open('res/multi_thread_200_10.json', 'rt', encoding='utf-8') as fp:
        res = json.load(fp)

    if is_sort:
        res['res'].sort(key=lambda i: i.get('total_delay') if i.get('total_delay') else 0)

    y = [i['total_delay'] for i in res['res'] if i['success']]
    y1 = [i['get_res_delay'] for i in res['res'] if i['success']]
    y2 = [i['upload_delay'] for i in res['res'] if i['success']]
    x = range(1, len(y) + 1)

    mpl.rcParams['font.sans-serif'] = ['Microsoft YaHei']
    mpl.rcParams['axes.unicode_minus'] = False
    pyplot.figure(figsize=(16, 8))
    pyplot.ylabel('响应时间（s）')
    pyplot.xlim((0, 2005))
    pyplot.ylim((0, 17))
    ax = pyplot.gca()
    x_major_locator = MultipleLocator(100)
    y_major_locator = MultipleLocator(1)
    ax.xaxis.set_major_locator(x_major_locator)
    ax.yaxis.set_major_locator(y_major_locator)

    if is_sort:
        pyplot.plot(x, y, '.k', label='总响应时间')
        pyplot.plot(x, y1, '_k', label='获取结果响应时间')
        pyplot.plot(x, y2, '+k', label='上传响应时间')

        pyplot.title('200 线程连续请求响应时间（按总响应时间升序排列）')
        pyplot.xlabel('数据编号')
        pyplot.legend(loc='upper left')

        pyplot.savefig('res/multi_thread_sorted_200_10.png')
    else:
        pyplot.plot(x, y, '-k', label='总响应时间')
        pyplot.plot(x, y1, '--k', label='获取结果响应时间')
        pyplot.plot(x, y2, ':k', label='上传响应时间')

        pyplot.title('200 线程连续请求响应时间（按请求先后顺序排列）')
        pyplot.xlabel('数据序号')
        pyplot.legend(loc='upper left')

        pyplot.savefig('res/multi_thread_200_10.png')

    pyplot.show()

    total_delay = (numpy.min(y), numpy.mean(y), numpy.median(y), numpy.max(y))
    get_res_delay = (numpy.min(y1), numpy.mean(y1), numpy.median(y1), numpy.max(y1))
    upload_delay = (numpy.min(y2), numpy.mean(y2), numpy.median(y2), numpy.max(y2))

    less_than_5s_count = 0
    for i in y:
        if i <= 5:
            less_than_5s_count += 1

    print(total_delay)
    print(get_res_delay)
    print(upload_delay)
    print(len(y), less_than_5s_count)


if __name__ == '__main__':
    # single_thread_data(False)
    # single_thread_data(True)
    # multi_thread_data(False)
    multi_thread_data(True)
