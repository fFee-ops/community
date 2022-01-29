package com.sl.community;

import com.sl.community.entity.DiscussPost;
import com.sl.community.dao.DiscussPostMapper;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootTests {

    private DiscussPost data;

    @Autowired
    private DiscussPostMapper discussPostMapper;


    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    @Before
    public void before(){//在每个测试方法执行之前都会构造data，不依赖数据库中的数据
        System.out.println("before");
        data=new DiscussPost();
        data.setId(111);
        data.setContent("Test");
        data.setScore(100.0);
        discussPostMapper.insertDiscussPost(data);

    }

    @After
    public void after(){//在每个测试方法执行之后，删除测试数据（状态改为2）
        System.out.println("after");
        discussPostMapper.updateDiscussStatus(data.getId(),2);
    }


    @Test
    public void test1(){
        System.out.println("test1");
    }

    @Test
    public void test2(){
        System.out.println("test2");
    }

    @Test
    public void testFindDiscussPost(){
        DiscussPost post = discussPostMapper.selectDiscussById(data.getId());
        //用断言判断得到的结果是否正确(你期望得到的数据，真实的数据)
        Assert.assertNotNull(post);
        Assert.assertEquals(data.getContent(),post.getContent());
    }

    @Test
    public void testUpdateScore(){
        int row = discussPostMapper.updateDiscussScore(data.getId(), 200.00);
        Assert.assertEquals(1,row);

        DiscussPost post = discussPostMapper.selectDiscussById(data.getId());
        Assert.assertEquals(200.00,post.getScore(),2);
    }
}
