import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/23  下午3:03.
 */
public class 直接插入排序 {

    /**
     * 1、直接插入排序 ：个人理解，就是往后移动，依次把小的放到前面来
     */
    @Test
    public  void insertionSort() {
        int[] a = { 49, 38, 65, 97, 76, 13, 27, 50 };
        System.out.println("----------插入排序开始：---------");
        print(a);
        int tmp;
        for (int i = 1; i < a.length; i++) {//从i等于1开始表示a[1] 也即是从第二个数字开始进行比较，进行n-1趟排序
            for (int j = i; j > 0; j--) { //将j赋值给i，也就是将当前未排序数据的位置赋值给j，进行已经有序队列中，的插入 从后往前比较所以是j--
                if (a[j] < a[j - 1]) { //进行从小到大的排序，然后进行赋值 职业的话，就能够得到有序数组a 和未排序的数组
                    tmp = a[j - 1];
                    a[j - 1] = a[j];
                    a[j] = tmp;
                }
            }
            System.out.printf("第"+i+"趟排序结果,");
            print(a);
        }

        System.out.print("最终插入排序结果： ");
        print(a);
        System.out.println("--------------------");
    }



    /**
     *
     打印的结果
     */
    private static void print(int []a) {
        for (int i : a){
            System.out.print(i + " ");
        }
        System.out.println();
    }




}
