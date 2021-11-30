import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/23  下午5:47.
 */
public class 快速排序 {



    /*
     * 4.快速排序 从两端向中间靠拢
     1．先从数列中取出一个数作为基准数。

     2．分区过程，将比这个数大的数全放到它的右边，小于或等于它的数全放到它的左边。

     3．再对左右区间重复第二步，直到各区间只有一个数。
     */
    public void QuickSort(int []a, int low, int high){

        int i=low,j=high;
        if(i<j){ //当i小于J的时候执行，也就是说low必须小于hign
            int po = a[low]; //po为基数
            while(i<j){ //每次当i比j小的时候小的时候开始比较，当它大于的时候，就会开始下一次排序
                while(i<j && po<a[j]){   //肯定是i<j的 ，一旦po小于后面的，那么j就减1， 从后往前推 j--
                                          //这里是while循环 ，一定到最后是po>a[j] 所以一定到了最后是i<j基本上毫无疑问的
                    j--;
                }
                if(i<j){ //通过上面的while，肯定需要交换了。
                    int temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                    i++; //i往前推进，交换完成i++
                }


                while(i<j && po>a[i]){   // 如果前面的大于后面的，肯定要推进的  从前往后推 i++
                    i++;
                }

                if(i<j){
                    int temp = a[i];
                    a[i] = a[j];
                    a[j] = temp;
                    j--;
                }
            }
            //这个时候i就比j大了，所以需要进行下一趟排序。直到最终的结果low =j-1 j+1=high
            QuickSort(a,low,j-1);   //从小到J 分成两组
            QuickSort(a,j+1,high);  //从J到到 分成两组
        }
    }

    /*
     * 4.快速排序  测试方法
     */
    @Test
    public void 快速排序算法(){
        int []a = { 49, 38, 65, 97, 76, 13, 27, 50 };
        int low = 0; //第一位
        int high = a.length-1; //最后一位
        QuickSort(a, low, high);
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
