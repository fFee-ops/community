package com.sl.community;

import com.sl.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 *
 * @date 2021/12/9 16:00
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitive(){
        String text="这里可以赌博，可以嫖娼，可以吸毒，可以玩耍。";
        /*text = sensitiveFilter.filter(text);
        System.out.println(text);
*/
        text="这里可以※赌★博★，可以★嫖★娼★，可以★吸&毒，可以玩耍。";
        text=sensitiveFilter.filter(text);
        System.out.println(text);

    }

}
