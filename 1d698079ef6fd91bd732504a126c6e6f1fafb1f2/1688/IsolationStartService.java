package com.hlj.springboot.dome.common.moudle.service;

import com.hlj.springboot.dome.common.entity.DemoEntity;
import com.hlj.springboot.dome.common.entity.OtherEntity;
import com.hlj.springboot.dome.common.entity.repository.DemoEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/1/23  下午8:02.
 * 类描述：
 */
@Service
public class IsolationStartService {

    @Resource
    private DemoEntityRepository demoEntityRepository ;

    @Resource
    private IsolationService isolationService ;


    @Transactional(isolation = Isolation.READ_COMMITTED ,propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public DemoEntity startTransactional(Long id) {

        //数据库中 id = 1 name = 0000 balance = 2222
        //数据库中 id = 2 name = aaaa balance = null

        //other id = 1 name = AAAA
        //other id = 2 name = BBBB

        //这个时候修改id为1的name为1111，以下查询还没有开启，表明我们的事物目前为止其实还没有开启，因为没有涉及到数据库的操作，有了数据库操作，才会真正开启事物
        DemoEntity demoEntity =  demoEntityRepository.findOne(id);
        demoEntity.setName("startTransactional"); //还没有保存到数据库中，因为事物还没有提交，方法运行结束才会提现目前开启的这个事物，除非中间出现独立事物
        System.out.println(demoEntity);     //DemoEntity(id=1, name=startTransactional, balance=2222)

        //这个时候我们创建一条id为3的数据
        DemoEntity demoEntity3 =  demoEntityRepository.findOne(3L);
        System.out.println(demoEntity3);


        //开始独立事物直接保存数据，直接在数据库中有显示
        DemoEntity transRequirsNew =   isolationService.transRequirsNew(id,"transRequirsNew");
        System.out.println(transRequirsNew); //DemoEntity(id=1, name=transRequirsNew, balance=2222)

        //解释：即使独立事物修改过，我们这里还是不会变，还是本事物中的数据(因为是两个事物，具有隔离性)
        System.out.println(demoEntity);  //DemoEntity(id=1, name=startTransactional, balance=2222)



        // 目前，直接看数据库中，name应该能是transRequirsNew， 手动修改数据库中的 name为 3333
        //读取已经提交的数据,这里没有开启独立事物，所以即使配置了，读已提交也不会生效，因为再我们这个方法一开始的时候，就已经确定了隔离性质
        demoEntity = isolationService.isoLationReadCommitedFind(id);
        System.out.println(demoEntity); // DemoEntity(id=1, name=transRequirs, balance=2222)

        //只在读已提交测试下面的2行代码,用来看看sql语句会不会造成影响，结果还是不会造成影响，不会读取其他事物修改的最新数据，因为本条记录我们已经拿下了
        demoEntity = isolationService.sqlFind(id);
        System.out.println(demoEntity); // DemoEntity(id=1, name=transRequirs, balance=2222)


        // 可重复读 这个时候我们修改 2中的数据为 bbbb，会发现这个时候查询的时候，下面还为aaa ，
        // READ_COMMITTED 可重复读取：修改了之后，我们查询到的数据库是bbbb，说明READ_COMMITTED 生效了
        DemoEntity two = isolationService.transRequirsFind(2L);
        System.out.println(two) ;
        //可重复读          DemoEntity(id=2, name=aaaa, balance=null)
        //READ_COMMITTED   DemoEntity(id=2, name=bbbb, balance=null)


        //下面一起修改了即使换一个变量名字，demoEntity的变量还是会跟着变的
        DemoEntity one = isolationService.transRequirs(1L,"transRequirsOne");
        System.out.println(one) ; //DemoEntity(id=1, name=transRequirsOne, balance=2222)
        System.out.println(demoEntity) ; //DemoEntity(id=1, name=transRequirsOne, balance=2222)

        //可重复读 这个时候直接修改 这张表中的数据为CCCC，我们会发现本事物中查询的还是之前的结果AAAA,说明本事物开启之后，就会对数据库中的数据进行锁定（相当于是拍了张照片）
        //READ_COMMITTED 这个时候直接修改数据库 这张表中的数据为CCCC， 这里查询得到的是CCCC，说明不可重复读生效了
        OtherEntity otherEntity = isolationService.findOther(1L);
        System.out.println(otherEntity);
        //可重复读 OtherEntity(id=1, name=AAAA)
        //READ_COMMITTED OtherEntity(id=1, name=CCC)


        //可重复读 这个时候直接修改 这张表中的数据为DDDD，我们会发现本事物中查询的还是之前的结果BBBB
        //READ_COMMITTED 这个时候直接修改数据库 这张表中的数据为CCCC， 这里查询得到的是CCCC，说明不可重复读生效了
        OtherEntity otherEntityTwo = isolationService.findOther(2L);
        System.out.println(otherEntityTwo);
        //可重复读 OtherEntity(id=2, name=BBBB)
        //READ_COMMITTED OtherEntity(id=2, name=DDDD)


        //指定id更新不会影响demoEntity 现在的数据
        isolationService.updateName(id,"zhang123456" );
        System.out.println(demoEntity); //DemoEntity(id=1, name=transRequirsOne, balance=2222)

        System.out.println(transRequirsNew);//DemoEntity(id=1, name=transRequirsNew, balance=2222)
        return demoEntity;

        //上面两种的最终数据库中就 都变成了
        // id =1 name= zhang123456  balance = 2222
        // id =2 name= bbb balance = nulll

        //other表 id= 1 name =CCCC
        //id = 2 name =DDDD
    }


}
