package com.hlj.springboot.dome.common.moudle.service.impl;

import com.hlj.springboot.dome.common.entity.DemoEntity;
import com.hlj.springboot.dome.common.entity.repository.DemoEntityRepository;
import com.hlj.springboot.dome.common.moudle.service.DemoEntityService;
import com.hlj.springboot.dome.common.moudle.service.PostConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

/**
 * @Desc: 方法中全部开启了aop 也就是启动了session控制
 * @Author HealerJean
 * @Date 2018/9/17  下午2:39.
 */
@Service
@Slf4j
public class DemoEntityServiceImpl implements DemoEntityService {

    @Resource
    DemoEntityRepository demoEntityRepository ;

    @Resource
    private EntityManager entityManager ;



    @Override
    public void testNoHaveSaveButSaveSuccess() {


        DemoEntity demoEntity =demoEntityRepository.findOne(1L);
        demoEntity.setBalance(12L);
        demoEntity.setName("张宇晋"); //观察到下面没有save方法，但是这里实实在在显示到数据库中去了

//        DemoEntity copy = demoEntity ;
//        copy.setName("copyEntity");

        entityManager.persist(demoEntity);


        DemoEntity demoEntityT = new DemoEntity();
        demoEntityT.setName("z1");
        demoEntityRepository.save(demoEntityT);

        demoEntityT.setBalance(12L); //会进行更新

        System.out.println(demoEntity);
        System.out.println(demoEntityT);


    }

    /**
     * 托管状态
      临时状态在调用persist()后，即可将一般的JavaBean做为了托管状态的Bean，该Bean的任何属性改动都会牵涉到数据库记录的改动。

       一旦该记录flush到数据库之后，并且事务提交了，那么此对象不在持久化上下文中，即：变为了游离（没人管的孩子）状态了。

      在游离状态的时候调用更新、刷新方法后，游离状态对象就变为了在持久化上下文的托管状态了。

      通过管理器的find方法，将实体从数据库查询出来后，该实体也就变为了托管形态。


      persist方法：使对象由临时状态变为持久化状态,就是执行INSERT操作。

      1.如果传递进persist()方法的参数不是实体Bean，会引发IllegalArgumentException
      2.和hibernate的save()方法有些不同：如果对象有id，则不能执行insert操作，会抛出异常
     */

    @Override
    public void persist() {
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setName("persistNoId");
        entityManager.persist(demoEntity); //会向数据库中插入数据，可以获取到id
        System.out.println(demoEntity); //DemoEntity(id=1, name=persistNoId, balance=null)

        demoEntity.setBalance(1500l); //会保存到数据库中，持久化操作 DemoEntity(id=1, name=persistNoId, balance=1500)
    }

    @Override
    public void persistHaveId() {
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setName("persist");
        demoEntity.setId(1L);
        entityManager.persist(demoEntity); //本来以为可以：更新id为1的数据。但是没有成功，失败。抛出异常，传入id就会抛出异常

    }

    /**
     * merge 简单来说，就是更新操作。
     *
     * 1.      传入的对象没有id
     * 在这种情况下，调用merge方法，将返回一个新的对象（有id），并对这个新的对象执行insert操作。
     *
     * 2.  传入的对象有id，entityManager的缓存中没有该对象，数据库中没有该记录:
     *
     * 在这种情况下，调用merge方法，将返回一个新的对象，并对该对象执行insert操作。
     *
     * 新对象的id是数据库中这条记录的id（比如自增长的id），而不是我们自己传入的id。（其实和情况1的结果是一样的）
     *
     * 3.      传入的对象有id，entityManager的缓存没有该对象，数据库中有该记录
     *
     * 在这种情况下，调用merge方法，将会从数据库中查询对应的记录，生成新的对象，然后将我们传入的对象复制到新的对象，最后执行update操作。
     *
     *
     * ---------------------

     */
    @Override
    public void merge() {
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setName("merge");
        entityManager.merge(demoEntity);////会向数据库中插入数据,但是获取不到id
        System.out.println(demoEntity); //DemoEntity(id=null, name=merge, balance=null)

        demoEntity.setBalance(1500l); //不会保存到数据库中，这里表示已经变成了托管状态，因为id，也没，去哪里保存啊，笨死了
                                      // DemoEntity(id=null, name=merge, balance=null)

    }
    @Override
    public void mergeHaveId() {
        DemoEntity demoEntity110 = new DemoEntity();
        demoEntity110.setName("demoEntity110");
        demoEntity110.setId(110L); //id在数据库中不存在
        entityManager.merge(demoEntity110);////会向数据库中插入数据, 新对象的id是数据库中这条记录的id（比如自增长的id），而不是我们自己传入的id。

        DemoEntity demoEntityID = new DemoEntity();
        demoEntityID.setName("demoEntityID");
        demoEntityID.setId(1L); //id在数据库中存在
        entityManager.merge(demoEntityID);//，将会从数据库中查询对应的记录，生成新的对象，然后将我们传入的对象复制到新的对象，最后执行update操作。

        System.out.println(demoEntity110); //DemoEntity(id=110, name=demoEntity110, balance=null) (并没有显示数据库中的数据)
        System.out.println(demoEntityID); //DemoEntity(id=1, name=demoEntityID, balance=null) 并没有显示数据库中的数据
    }

    /**
     * remove对象必须是数据库中获取的
     */
    @Override
    public void remove() {
        DemoEntity demoEntity110 = new DemoEntity();
        demoEntity110.setName("demoEntity110");
        demoEntity110.setId(4L); //id在数据库中不存在

//        entityManager.remove(demoEntity110); //异常

        DemoEntity data = demoEntityRepository.findOne(4l);
        entityManager.remove(data);//从数据库中直接删除

        System.out.println(data); //DemoEntity(id=4, name=4, balance=4)
    }

    @Override
    public void refresh() {
        DemoEntity demoEntity = new DemoEntity();
        demoEntity.setName("refresh");
//        entityManager.merge(demoEntity);//没有显示数据库中的数据
//        entityManager.refresh(demoEntity); 报错，必须是数据库中直接获取的

        demoEntity = demoEntityRepository.findOne(1l);
        demoEntity.setName("refresh");
        entityManager.refresh(demoEntity); //表示和数据库中同步，呈现的是数据库中的数据，注意下面的name不是refresh，所以这个name值refresh 不会保存到数据库中(因为这个方法的事物还没有执行，上面的还没完全保存到数据库中去)
        System.out.println(demoEntity); //DemoEntity(id=1, name=demoEntityID, balance=null)

        demoEntity.setBalance(1500l); //会报错到数据库中去， 也就是说数据库最终会变成 id 1 name demoEntityID balance 1500

        DemoEntity persist = new DemoEntity();
        persist.setName("persist");
        entityManager.persist(persist);//最终显示的是数据库中的数据
        entityManager.refresh(persist); //表示和数据库中同步，注意下面出现了数据库中刚刚插入的id
        System.out.println(persist); //DemoEntity(id=6, name=persist, balance=null)


    }

}
