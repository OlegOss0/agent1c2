package e.sergeev.oleg.agent1c2;

//It isn't used in the working version, only for tests of removal of coordinates
public class DataBaseStatic {
    static DataBaseHelper dataBaseHelper;

    public DataBaseStatic(DataBaseHelper dataBaseHelper) {
        this.dataBaseHelper = dataBaseHelper;
    }

    public DataBaseStatic (){
    }


    public static DataBaseHelper getDataBaseHelper() {
        return dataBaseHelper;
    }
}
