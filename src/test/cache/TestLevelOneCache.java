package test.cache;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import test.pojo.User;

import java.util.Date;

/**
 * Created by root on 17-11-27.
 */
public class TestLevelOneCache {
    private SessionFactory factory;

    @Before
    public void init(){
        factory = new Configuration().configure().buildSessionFactory();
    }

    @After
    public void destory(){
        factory.close();
    }

    //以及缓存是基于session的缓存,不可关闭
    @Test
    //同一个session,同一个session,get同一个对象只会发出一次sql语句
    //测试1,同session同transaction
    public void testSessionCache1(){
        Session session1 = factory.openSession();
        Transaction transaction1 = null;
        try{

            transaction1 = session1.beginTransaction();
            User user1 = session1.get(User.class,1);
            User user2 = session1.get(User.class,1);
            transaction1.commit();
            System.out.println(user1 == user2);


        }catch (HibernateException e){
            if(transaction1 != null){
                transaction1.rollback();
            }
            e.printStackTrace();
        }finally {
            session1.close();
        }
    }

    @Test
    //测试2,同session不同transaction,一般一个session对应一个事务，不会产生并发问题,这里两个事务只是测试
    public void testSessionCache2(){
        Session session1 = factory.openSession();
        Transaction transaction1 = null;
        Transaction transaction2 = null;
        try{
            transaction1 = session1.beginTransaction();
            User user1 = session1.get(User.class,1);
            transaction1.commit();


            transaction2 = session1.beginTransaction();
            User user2 = session1.get(User.class,1);
            transaction2.commit();

            System.out.println("transaction1 == transaction2 : " + (transaction1 == transaction2));
            System.out.println("user1 == user2 : " + (user1 == user2));//true
        }catch (HibernateException e){
            if(transaction1 != null){
                transaction1.rollback();
            }
            if(transaction2 != null){
                transaction2.rollback();
            }

            e.printStackTrace();
        }finally {
            session1.close();
        }
    }

    @Test
    //insert操作也会触发Session缓存,故没有产生select语句,两个对象为同一个对象
    public void testSessionCache3(){
        Session session1 = factory.openSession();
        Transaction transaction1 = null;
        try{
            User user1 = new User("benben",new Date());
            transaction1 = session1.beginTransaction();
            Integer id = (Integer) session1.save(user1);
            User user2 = session1.get(User.class,id);
            transaction1.commit();
            System.out.println(user1 == user2);

        }catch (HibernateException e){
            if(transaction1 != null){
                transaction1.rollback();
            }
            e.printStackTrace();
        }finally {
            session1.close();
        }
    }

    @Test
    //不同session
    public void testSessionCache4(){
        Session session1 = factory.openSession();
        Session session2 = factory.openSession();
        Transaction transaction1 = null;
        Transaction transaction2 = null;
        try{
            transaction1 = session1.beginTransaction();
            User user1 = session1.get(User.class,1);
            transaction1.commit();


            transaction2 = session2.beginTransaction();
            User user2 = session2.get(User.class,1);
            transaction2.commit();
            System.out.println("user1 == user2 : " + (user1 == user2));//false

        }catch (HibernateException e){
            if(transaction1 != null){
                transaction1.rollback();
            }
            if(transaction2 != null){
                transaction2.rollback();
            }

            e.printStackTrace();
        }finally {
            session1.close();
        }
    }

    @Test
    //清除session中的某个对象的缓存
    public void testEvict(){
        Session session1 = factory.openSession();
        Transaction transaction1 = null;
        try{
            User user1 = new User("benben",new Date());
            transaction1 = session1.beginTransaction();
            //存入
            Integer id = (Integer) session1.save(user1);
            //清楚user1对象在session中的缓存
            session1.evict(user1);
            //因为没有缓存了,这里得到的是新对象
            User user2 = session1.get(User.class,id);
            transaction1.commit();
            System.out.println(user1 == user2);

        }catch (HibernateException e){
            if(transaction1 != null){
                transaction1.rollback();
            }
            e.printStackTrace();
        }finally {
            session1.close();
        }
    }

    @Test
    //清除session中的所有对象的缓存
    public void testClear(){
        Session session1 = factory.openSession();
        Transaction transaction1 = null;
        try{
            transaction1 = session1.beginTransaction();

            //因为没有缓存了,这里得到的是新对象
            User user1 = session1.get(User.class,1);
            User user2 = session1.get(User.class,2);

            User user3 = session1.get(User.class,1);
            User user4 = session1.get(User.class,2);

            System.out.println(user1 == user3);
            System.out.println(user2 == user4);

            session1.clear();

            User user5 = session1.get(User.class,1);
            User user6 = session1.get(User.class,2);

            System.out.println(user1 == user5);
            System.out.println(user2 == user6);
            transaction1.commit();


        }catch (HibernateException e){
            if(transaction1 != null){
                transaction1.rollback();
            }
            e.printStackTrace();
        }finally {
            session1.close();
        }
    }
}
