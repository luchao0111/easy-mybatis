import com.alibaba.fastjson.JSON;
import com.cehome.easymybatis.*;
import org.apache.ibatis.session.RowBounds;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class Test1 {

    @Autowired
    DataSource dataSource;

    @Autowired
    UserMapper userMapper;

    @Autowired
    SqlSessionTemplate sqlSessionTemplate;

    Long id=36L;
    Integer age=25;
    String name="ma";
    String realName="coolma";
    @Test
    public void testSelect() throws SQLException {
        getByEntity();
        getUser();
        getById();
        getValueByEntity();
        getValueByWhere();
        listByEntity();
        listBySQL();
    }
    @Test
    public void testUpdate() throws SQLException {
        insert();
        update();
        updateByEntity();
        updateByWhere();

    }

    @Test
    public void testDelete() throws SQLException {
        insert();delete();
        insert();deleteById();
        insert();deleteByEntity();
        insert();deleteByWhere();

    }

    @Test
    public void insert() throws SQLException {


        User user=new User();

        user.setName(name);
        user.setAge(age);
        user.setRealName(realName);
        user.put("email", "'a@a.com'");

        //user.setCreateTime(new Date(System.currentTimeMillis()+1000*3600));
        userMapper.insert(user);

        System.out.println(JSON.toJSONString(user));
        Assert.assertNotNull(user.getId());
        id=user.getId();

        //sql.append("insert into "+beanAnn.getTable());



    }
    @Test
    public void update() throws SQLException {
        User user=new User();
        user.setName("updateById");
        user.setId(id);
        user.put("createTime","now()");
        Assert.assertEquals(1, userMapper.update(user));
    }
    @Test
    public void updateByEntity() throws SQLException {

        User user=new User();
        user.setName("updateByEntity");
        user.setCreateTime(new Date());
        user.setEmail("ube@a.com");

        User where=new User();
        where.setId(id);
        where.setAge(age);
        Assert.assertEquals(1,userMapper.updateByEntity(user,where));

    }
    @Test
    public void updateByWhere() throws SQLException {

        User user=new User();
        user.setCreateTime(new Date());
        user.setEmail("usa@a.com");

        String where="{id}=#{id} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("id",id);
        map.put("realName",realName);

        int row= userMapper.updateByWhere(user,where,map);
        System.out.println(row);

    }

    @Test
    public void delete() throws SQLException {
        //System.out.println(dataSource.getConnection().getMetaData().getURL());
        User user=new User();
        user.setId(id);
        Assert.assertEquals(1,userMapper.delete(user));

    }
    @Test
    public void deleteById() throws SQLException {
        Assert.assertEquals(1,userMapper.deleteById(id));


    }
    @Test
    public void deleteByEntity() throws SQLException {

        User params=new User();
        params.setName(name);
        params.setAge(age);
        Assert.assertEquals(1,userMapper.deleteByEntity(params));


    }
    @Test
    public void deleteByWhere() throws SQLException {


        String where="{name}=#{name} and {realName}=#{realName}";
        Map map=new HashMap();
        map.put("name",name);
        map.put("realName",realName);

        int row= userMapper.deleteByWhere(where,map);
        Assert.assertEquals(1,row);

    }


    @Test
    public void findById() throws SQLException {

        User user=userMapper.findById(36L);
        System.out.println(JSON.toJSONString(user));
    }


    @Test
    public void getByEntity() throws SQLException {
        User params=new User();
        params.setId(id);
        User user=userMapper.getByEntity(params,null);
        verify(user,id);

    }

    @Test
    public void getUser() throws SQLException {
         User user=userMapper.getUser(id);
        verify(user,id);
    }
    private void verify(User user,Long id){
        System.out.println(JSON.toJSONString(user));
        Assert.assertEquals(user.getId(),id);
        Assert.assertNotNull(user.getName());
    }

    @Test
    public void getById() throws SQLException {
        //System.out.println(dataSource.getConnection().getMetaData().getURL());

        User user=userMapper.getById(id,null);
        verify(user,id);
    }



    @Test
    public void getValueByEntity() throws SQLException {
        User params=new User();
        params.setId(id);
        Object value=userMapper.getValueByEntity(params,"name");
        System.out.println(JSON.toJSONString(value));
        Assert.assertNotNull(value);


    }
    @Test
    public void getValueByWhere() throws SQLException {
        {
            User params = new User();
            params.setId(36L);
            Object value = userMapper.getValueByWhere( "{id}=#{id}", params,"name");
            System.out.println(JSON.toJSONString(value));
            Assert.assertNotNull(value);
        }

        {
            Map<String,Object> params=new HashMap();
            params.put("id",36L);
            Object value = userMapper.getValueByWhere( "{id}=#{id}", params,"name");
            System.out.println(JSON.toJSONString(value));
            Assert.assertNotNull(value);
        }

    }

    @Test
    public void listByEntity() throws SQLException {
        User params=new User();
        params.setAge(20);
        List<User> list=userMapper.listByEntity(params," name asc, createTime desc","age,createTime");
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);

    }

    @Test
    public void pageByEntity() throws SQLException {
        User params=new User();
        params.setAge(20);
        Page<User> page=new Page(1,3);
        List<User> list=userMapper.pageByEntity(params,page," name asc, createTime desc","age,createTime");
        System.out.println(page.getData().size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(page.getData().size()>0);

    }

    @Test
    public void listBySQL() throws SQLException {
        User params=new User();
        params.setAge(20);
        List<User> list=userMapper.listBySQL(" age>#{age} order by {createTime} desc",params);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void pageBySQL() throws SQLException {
        User params=new User();
        params.setAge(20);
        Page<User> page=new Page(2,5);
        List<User> list=userMapper.pageBySQL(" age>#{age} order by {createTime} desc",params,page);
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(page));
        Assert.assertTrue(list.size()>0);
    }

    @Test
    public void getPage() throws SQLException {

        List<User> list=userMapper.getPage(2,new RowBounds(3,4));
        System.out.println(list.size()+"\r\n"+JSON.toJSONString(list));
        Assert.assertTrue(list.size()>0);
    }


    @Test
    public void test2(){
        SqlMapper sqlMapper=new SqlMapper(sqlSessionTemplate);
        System.out.println(JSON.toJSONString(sqlMapper.selectOne("SELECT * FROM user WHERE id = #{id}",36L)));
    }

}