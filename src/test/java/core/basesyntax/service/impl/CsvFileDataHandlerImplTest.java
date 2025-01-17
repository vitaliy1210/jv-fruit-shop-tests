package core.basesyntax.service.impl;

import static org.junit.Assert.assertEquals;

import core.basesyntax.dao.FruitsDao;
import core.basesyntax.dao.FruitsDaoImpl;
import core.basesyntax.db.Storage;
import core.basesyntax.operations.OperationHandler;
import core.basesyntax.operations.OperationStrategy;
import core.basesyntax.operations.OperationStrategyImpl;
import core.basesyntax.operations.impl.BalanceHandler;
import core.basesyntax.operations.impl.PurchaseHandler;
import core.basesyntax.operations.impl.ReturnHandler;
import core.basesyntax.operations.impl.SupplyHandler;
import core.basesyntax.service.CsvFileDataHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

public class CsvFileDataHandlerImplTest {
    private static List<String> fileData;
    private static FruitsDao fruitsDao;
    private static CsvFileDataHandler dataHandler;

    @BeforeClass
    public static void setUp() {
        fruitsDao = new FruitsDaoImpl();
        Map<String, OperationHandler> operations = new HashMap<>();
        operations.put("b", new BalanceHandler(fruitsDao));
        operations.put("s", new SupplyHandler(fruitsDao));
        operations.put("r", new ReturnHandler(fruitsDao));
        operations.put("p", new PurchaseHandler(fruitsDao));
        OperationStrategy operationStrategy = new OperationStrategyImpl(operations);
        dataHandler = new CsvFileDataHandlerImpl(operationStrategy);
        fileData = new ArrayList<>();
        fileData.add("type,fruit,quantity");
    }

    @Test
    public void processData_checkAllOperations_ok() {
        fileData.add("b,banana,20");
        fileData.add("b,apple,100");
        fileData.add("s,banana,100");
        fileData.add("p,banana,13");
        fileData.add("r,apple,10");
        fileData.add("p,apple,20");
        fileData.add("p,banana,5");
        fileData.add("s,banana,50");
        dataHandler.processData(fileData);
        assertEquals(152, fruitsDao.getAmount("banana"));
        assertEquals(90, fruitsDao.getAmount("apple"));
        assertEquals(2, fruitsDao.getFruitsNames().length);
    }

    @Test(expected = RuntimeException.class)
    public void processData_tooMuchPurchase_notOk() {
        fileData.add("p,banana,200");
        dataHandler.processData(fileData);
    }

    @Test(expected = RuntimeException.class)
    public void processData_nonexistentFruitPurchase_notOk() {
        fileData.add("p,strawberry,20");
        dataHandler.processData(fileData);
    }

    @After
    public void afterEachTest() {
        fileData.remove(fileData.size() - 1);
        Storage.fruits.clear();
    }
}
