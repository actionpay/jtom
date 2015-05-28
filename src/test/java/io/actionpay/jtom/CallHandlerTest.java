package io.actionpay.jtom;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Temp on 18.05.2015.
 */
public class CallHandlerTest {

    class TestQueryResult implements QueryResult {
        List data = new ArrayList();

        public TestQueryResult() {
        }

        public TestQueryResult(Collection data) {
            this.data.addAll(data);
        }

        @Override
        public List getAsPlainList() {
            return data;
        }

        @Override
        public List getAsObjectList() throws Exception {
            return data;
        }

        @Override
        public Iterator getPlainIterator() {
            return data.iterator();
        }

        @Override
        public Iterator getObjectIterator() throws Exception {
            return data.iterator();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof QueryResult))
                return false;
            QueryResult cmp = (QueryResult) obj;
            return data.containsAll(cmp.getAsPlainList()) && cmp.getAsPlainList().containsAll(this.data);
        }
    }

    public QueryResult mockNonModifyMethod(QueryResult args){
        return args;
    }

    public QueryResult mockModifySameDataMethod(QueryResult args){
        args.getAsPlainList().add("modyfy");
        return args;
    }


    public QueryResult mockModifyMethod(QueryResult args){
        QueryResult result = new TestQueryResult(args.getAsPlainList());
        result.getAsPlainList().add("modyfy");
        return result;
    }

    @Test
    public void testNonModify() throws Exception {
        CallHandler handler = new CallHandlerImpl();
        Method methodNonModify = getClass().getDeclaredMethod("mockNonModifyMethod", QueryResult.class);
        handler.registerHandler("nonModify", methodNonModify);
        QueryResult result = new TestQueryResult();
        QueryResult noModifyResult = (QueryResult)handler.callHandler("nonModify", this, result);
        Assert.assertEquals(result, noModifyResult);
    }

    @Test
    public void testModify() throws Exception {
        CallHandler handler = new CallHandlerImpl();
        Method methodModify = getClass().getDeclaredMethod("mockModifyMethod", QueryResult.class);
        handler.registerHandler("modify", methodModify);
        QueryResult result = new TestQueryResult();
        TestQueryResult modifyResult = (TestQueryResult) handler.callHandler("modify", this, result);
        Assert.assertNotEquals(result, modifyResult);
    }


    @Test
    public void testSameDataModify() throws Exception {
        CallHandler handler = new CallHandlerImpl();
        Method methodModifySameData = getClass().getDeclaredMethod("mockModifySameDataMethod", QueryResult.class);
        handler.registerHandler("modifySameData", methodModifySameData);
        QueryResult result = new TestQueryResult();
        TestQueryResult modifySameDataResult = (TestQueryResult) handler.callHandler("modifySameData", this, result);
        Assert.assertEquals(result, modifySameDataResult);
    }

    @Test
    public void testMethodNotExist(){
        try {
            new CallHandlerImpl().callHandler("non exist method", this, null);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultiRegMultiCall() throws Exception {
        CallHandler handler = new CallHandlerImpl();
        Method methodNoModify = getClass().getDeclaredMethod("mockNonModifyMethod", QueryResult.class);
        Method methodModify = getClass().getDeclaredMethod("mockModifyMethod", QueryResult.class);

        //test register try
        handler.registerHandler("modify", methodNoModify);
        handler.registerHandler("modify", methodModify);
        QueryResult result = new TestQueryResult();
        result = (QueryResult)handler.callHandler("modify", this, result);
        Assert.assertEquals(result.getAsPlainList().size(), 1);
        result.getAsPlainList().clear();
        handler.registerHandler("modify", methodModify);
        result = (QueryResult)handler.callHandler("modify", this, result);
        Assert.assertEquals(result.getAsPlainList().size(), 2);
        result.getAsPlainList().clear();
        result = (QueryResult)handler.callHandler("modify", this, result);
        result = (QueryResult)handler.callHandler("modify", this, result);
        Assert.assertEquals(result.getAsPlainList().size(), 4);
    }



}
