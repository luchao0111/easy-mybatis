package com.cehome.easymybatis;

import com.cehome.easymybatis.utils.Utils;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * coolma 2019/11/1
 **/
@Intercepts({
        @Signature(
                type= Executor.class,
                method = "update",
                args = {MappedStatement.class,Object.class}),
        @Signature(
                type= Executor.class,
                method = "query",
                args = {MappedStatement.class,Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(
                type= Executor.class,
                method = "query",
                args = {MappedStatement.class,Object.class,RowBounds.class, ResultHandler.class}),
})

public class SimpleInterceptor implements Interceptor {

    private Map<String,MappedStatement> countMap=new ConcurrentHashMap();
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取Executor对象query方法的参数列表
        final Object[] args = invocation.getArgs();
        // MappedStatement对象实例
        MappedStatement statement = (MappedStatement) args[0];




        //Object parameterObject=args[1];
        RowBounds rowBounds=(RowBounds)args[2];
        //MappedStatement ms, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler
        // 本次拦截的执行器对象

        SqlSource sqlSource= statement.getSqlSource();



         Page page=getPage(args[1]);


        if(statement.getSqlCommandType()==SqlCommandType.SELECT && page!=null){
           Object parameterObject= args[1];
           Executor executor = (Executor) invocation.getTarget();

           BoundSql boundSql = statement.getBoundSql(parameterObject);
           String sql = boundSql.getSql() + " limit ? offset ? ";
           List<ParameterMapping> pms = new ArrayList<ParameterMapping>();
           pms.addAll( boundSql.getParameterMappings());
           pms.add(new ParameterMapping.Builder(statement.getConfiguration(), "page.pageSize", Integer.TYPE).build());
           pms.add(new ParameterMapping.Builder(statement.getConfiguration(), "page.pageOffset", Integer.TYPE).build());
           BoundSql boundSql2 = new BoundSql(statement.getConfiguration(), sql, pms, parameterObject);
           CacheKey cacheKey = executor.createCacheKey(statement, parameterObject, rowBounds, boundSql2);
           //Class entityClass= EntityAnnotation.getInstanceByMapper(getMapperClass(statement.getId())).getEntityClass();
           List list = executor.query(statement, parameterObject, rowBounds, null, cacheKey, boundSql2);

           page.setData(list);


           boundSql2 = new BoundSql(statement.getConfiguration(), "select count(*) from ("+boundSql.getSql()+") a ", boundSql.getParameterMappings(), parameterObject);
             cacheKey = executor.createCacheKey(statement, parameterObject, rowBounds, boundSql2);
           int total= (Integer)executor.query(createMappedStatement(statement,Integer.class), parameterObject, rowBounds, null, cacheKey, boundSql2).get(0);
           page.setTotalRecord(total);
           page.setTotalPage((total-1)/page.getPageSize()+1);
           return list;


       }


        return invocation.proceed();
    }

    private Page getPage(Object arg){
        Page page=null;
        if(arg instanceof  MapperMethod.ParamMap ){
            MapperMethod.ParamMap parameterObject=( MapperMethod.ParamMap )arg;

            for(Object value :parameterObject.values()){
                if(value instanceof  Page){
                    page=(Page)value;
                    break;
                }
            }

        }
        return page;
    }
    private Class getMapperClass(String id){
        int n=id.lastIndexOf('.');
        String className=id.substring(0,n);
        String methodName=id.substring(n+1);

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private Method getMethod(String id){
        int n=id.lastIndexOf('.');
        String className=id.substring(0,n);
        String methodName=id.substring(n+1);
        Class c= null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
           throw new RuntimeException(e);
        }
        for(Method m: c.getMethods()){
           if(m.getName().equals(methodName)) return m;
        }
        return null;
    }

    private MappedStatement createMappedStatement(final MappedStatement statement, final Class resultTypeClass){
        String id=statement.getId()+"_count";
        MappedStatement result=countMap.get(id);
        if(result!=null) return result;
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(statement.getConfiguration(),

                id, statement.getSqlSource(), statement.getSqlCommandType())
                .resource(statement.getResource())
                .fetchSize(statement.getFetchSize())
                .timeout(statement.getTimeout())
                .statementType(statement.getStatementType())

                .databaseId(statement.getDatabaseId())
                .lang(statement.getLang())
                .resultOrdered(statement.isResultOrdered())
                .resultSets(Utils.toString( statement.getResulSets(),",",null))
                .resultMaps(new ArrayList() {
                    {
                        add(new ResultMap.Builder(statement.getConfiguration(), statement.getId(), resultTypeClass, new ArrayList()).build());
                    }
                })
                .resultSetType(statement.getResultSetType())
                .flushCacheRequired(statement.isFlushCacheRequired())
                .useCache(statement.isUseCache())
                .cache(statement.getCache());
        result=  statementBuilder.build();
        countMap.put(id,result);
        return result;
    }
}