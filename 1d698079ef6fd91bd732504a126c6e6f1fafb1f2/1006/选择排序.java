import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/23  下午4:04.
 */
public class 选择排序 {



    @Test
    public  void 选择排序算法(){
        int []a = { 49, 38, 65, 97, 76, 13, 27, 50 };

        int min;
        for(int i = 0; i<a.length-1;i++){ //从前往后比较，从0开始，是因为它要赋值给min,i一直到a.length 也就是最后一个还需要往前移动
            min = i; //首先默认第一个为min最小值
            for(int j = i+1 ; j<a.length;j++) { //以为是和min比较 ，不需要自己跟自己比较，min初始给的i，所以j=i+1;
                if(a[min]  > a[j]){
                    min = j; //每次都把最小的给min
                }
            }
            if(min!=i){//每趟排序之后，min的值都会不一样 ,而每次的min都是开始的i，所以当下的i和min进行替换
                int temp = a[min];
                a[min] = a[i];
                a[i] =temp;
            }
            print(a);
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
