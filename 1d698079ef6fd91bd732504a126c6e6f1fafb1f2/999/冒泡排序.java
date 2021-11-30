import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/23  下午6:39.
 */
public class 冒泡排序 {

    /**
     3、冒泡排序，其实就是将大的向后移动
     */
    @Test
    public  void maopao(){
        int str[] = { 49, 38, 65, 97, 76, 13, 27, 50 };
        System.out.println("----------冒泡排序 开始：---------");

        for (int i = 0; i <str.length-1; i++){    //最多做n-1趟排序
            for(int j = 0 ;j <str.length - i - 1; j++){    //对当前无序区间str[0......length-i-1]进行排序(j的范围很关键，这个范围是在逐步缩小的)
                if(str[j] > str[j + 1]){    //把小的值放到前面
                    int temp = str[j];
                    str[j] = str[j + 1];
                    str[j + 1] = temp;
                }
            }
            int n = i+1;
            System.out.printf("第"+n+"趟排序结果,");
            print(str);
        }
        System.out.print("最终排序结果：");
        print(str);


    }


    /*
     * 冒泡排序优化一
     * 设置一个标记来标志一趟比较是否发生交换
     * 如果没有发生交换，则数组已经有序
     * */
    @Test
    public void bubbleSort1() {
        int a[] = { 49, 38, 65,  76, 13, 27, 50 ,97};

        for (int i = 0; i <  a.length-1; ++i) {
            int  flag = 0;
            for ( int j = 0; j <  a.length - 1 - i; ++j) {
                if (a[j] > a[j + 1]) {
                    flag = 1;
                    int tmp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = tmp;
                }
            }
            if (flag == 0) { //当一趟比较没有发送交换的时间表示一件有序
                break;
            }
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
