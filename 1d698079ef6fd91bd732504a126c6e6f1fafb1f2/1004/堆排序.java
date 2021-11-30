import org.junit.Test;

import java.util.Arrays;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/26  下午3:44.
 */
public class 堆排序 {

    @Test
    public  void main() {
        int[] a = { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 };
        sort(a);
        System.out.println("排序后数组：" + Arrays.toString(a));
    }

    public  void sort(int[] a) {
        // 循环建立初始堆,若父节点索引为i，那么左节点的索引为i*2+1，即左节点为a.length时，其父节点应当小于a.length/2
        for (int i = a.length / 2; i >= 0; i--) {// 遍历存在子节点的父节点
            adjustHeap(a, i, a.length - 1);
        }

        // 进行n-1次循环完成排序
        for (int i = a.length - 1; i > 0; i--) {
            // 最后一个元素和第一个元素进行交换
            int temp = a[i];
            a[i] = a[0];
            a[0] = temp;

            adjustHeap(a, 0, i);
        }
    }

    // 将数组转换为大根堆，大根堆的根节点为数组中的最大值
    public  void adjustHeap(int[] a, int parent, int length) {
        int temp = a[parent]; // 父节点的值
        int child = 2 * parent + 1; // 左子节点的索引

        while (child < length) {// 判断左节点是否为最大索引，也就是说一开始进不来。
            // 如果有右孩子结点，并且右孩子结点的值大于左孩子结点，则选取右孩子结点
            if (child + 1 < length && a[child] < a[child + 1]) {
                child ++;// 将左子节点转换为右子节点
            }
            // 当父节点的值直接大于子节点的值时，直接退出
            if (temp > a[child]) {
                break;
            }
            // 将子节点的值赋值给父节点,这个时候表示子节点小于父节点，进行交换
            a[parent] = a[child];
            // 选取子节点的左子节点继续向下筛选
            parent = child;
            child = 2 * parent + 1;
        }
        // 若发生交换，此时parent代表子节点索引，没有发生交换，此时parent仍旧代表父节点索引
        a[parent] = temp;
    }


}
