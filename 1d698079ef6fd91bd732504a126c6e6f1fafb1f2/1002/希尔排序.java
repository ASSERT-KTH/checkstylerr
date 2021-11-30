import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/23  下午3:15.
 */
public class 希尔排序 {



    /**
     2、希尔排序.最小缩量排序  比如 8/8 个数字 4 2 1
     希尔排序是基于插入排序提出改进方法的：

     1、插入排序在对几乎已经排好序的数据操作时， 效率高， 即可以达到线性排序的效率，但插入排序一般来说是低效的， 因为插入排序每次只能将数据移动一位

     希尔排序的基本思想是：
        先将整个待排序的记录序列分割成为若干子序列分别进行直接插入排序，
       待整个序列中的记录“基本有序”时，再对全体记录进行依次直接插入排序。
     */


    @Test
    public void 希尔排序算法(){

        int[] a = { 49, 38, 65, 97, 76, 13, 27, 50,66 };
        System.out.println("----------希尔排序开始：---------");

        int incr = a.length/2; //希尔排序增量，//被分成4组 ，也即是第1个和第5个进行比较 ，低2个和低6个比较
        int temp ;
        while (incr>=1){ //当增量为0的时候排序完成
            for(int i = 0 ;i< a.length-1;i++){ //以为是从前往后第一个数字开始比较，所以初始化i=0 ，插入排序是从后往前比较
                for(int j = i; j < a.length-incr;j=j+incr){ // 这里的每一趟相当于是一次插入排序的排序算法，不同的是，这里是从前往后
                                                            // J的大小不会超过增量，而且因为每次都是j 和 j+incr
                                                            // 所以每趟都要J = j+incr,
                                                           // 当这个数字加起来超过 length-incr的时候，就完成一次比较
                    if(a[j]>a[j+incr]){
                        temp = a[j];
                        a[j] = a[j+incr];
                        a[j+incr]=temp;
                    }


                }
            }
            print(a);
            incr = incr/2;
        }
        print(a);


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
