/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/24  下午12:09.
 */
public class 折半查找 { //先有序，再折半查找
    public static void main(String[] args) {

        int a[]=new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,16,17,18,19,20};
        int low=0;
        int high=a.length-1;
        int mid;
        int x=20;
        while(low<=high){  //最后一定的是low和hig重合和x得坐标相等
            mid=(low+high)/2;
            if(a[mid]==x){
                System.out.println(x+"在数组中出现的位置"+mid);
                break;
            }
            if(a[mid]<x){
                low=mid+1;
            }
            if(a[mid]>x){
                high=mid-1;
            }
            if(low>high){
                System.out.println("查找失败");
                break;
            }
        }

    }

}
