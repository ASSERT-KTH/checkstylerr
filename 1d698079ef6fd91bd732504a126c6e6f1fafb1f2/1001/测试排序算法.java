import org.junit.Test;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/24  上午10:54.
 */
public class 测试排序算法 {


    /**
     * 直接插入排序思想是 依次走下去 ，然后将它放到前面的位置
     */
    @Test
    public void 直接插入排序(){
        int a[] = {2,123,12,45,23,234,4,5};
        for(int i=1;i<a.length-1;i++){
            for(int j = i;j>0;j--){

                if(a[j]>a[j-1]){
                    int temp = a[j];
                    a[j]= a[j-1];
                    a[j-1] = temp;
                }
            }
        }

        print(a);
    }


    @Test
    public void 选择排序(){ //每次选择一个小的放到左边的右面
        int a[] = {2,123,12,45,23,234,4,5};

        for(int i = 0;i<a.length-1;i++){
            int min = i;
            for(int j = i+1;j<a.length;j++){
                if(a[min]>a[j]){
                    min = j;
                }
            }

            if(min!=i){
                int temp = a[min];
                a[min]  = a[j];
                a[j] = temp;
            }
        }


        print(a);

    }


    /**
     * 快速排序为从两边逼近
     */
    @Test
    public void 快速排序(){
        int a[] = {2,123,12,45,23,234,4,5};

        int low =0;
        int high = a.length-1;
        快速排序method(a,low,high);
        print(a);


    }

    快速排序method(int a[],int low,int high){

        int i = low;
        int j = high;
        if(i<j){
            int po = a[low];
            while (i<j&&po<a[j]){
                j--;
            }
            if(i<j){
                int temp = a[i];
                a[j] = a[j];
                a[j] = temp;
                i++;
            }
            while (i<j&&po>a[i]){
                i++;
            }
            if(i<j){
                int temp = a[i];
                a[j] = a[j];
                a[j] = temp;
                j--;
            }

            快速排序method(a,low,j-1);
            快速排序method(a,j+1,high);

        }
    }

    @Test
    public void 希尔排序(){
        int a[] = {2,123,12,45,23,234,4,5};

        int low = 0;
        int high  = a.length-1;
        int incr = (low+high)/2;

        while (incr>=1) {
            for (int i = 0; i < a.length; i++) {
                for (int j = j; j < a.length - incr; j = j + incr) ;
                if(a[j] > [j+incr]){
                    int temp = a[j]；
                    a[j] = a[j+incr];
                    a[j+incr] = temp;
                }
            }
            incr = incr/2;

        }

        print(a);

    }


    @Test
    public void 希尔排序加折半查找(){

        int a[] = {2,123,12,45,23,234,4,5};


        //折半查找
        int low  = 0;
        int high  = a.length;
        int x = 123;
        while (low<=high){
            mid  = (low+high)/2;
            if(a[mid] == x){
                System.out.println(mid);
            }
            if(x>a[mid]){
                low = mid;
            }
            if(x<a[mid]){
                high = mid;
            }
            else{
                System.out.println("查无此数");
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
