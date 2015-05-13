import org.adonweb.odm.Entity;
import org.adonweb.odm.Field;
import org.adonweb.odm.Key;

/**
 * Created by Temp on 12.05.2015.
 */
@Entity(space="test_entity"
        , connection = "keeper")
public class MockEntity {
    @Key(index=0,position = 0)
    @Field(position = 0)
    private Integer id;

    @Field(position = 1)
    private Integer f1;

    @Field(position = 10)
    private Integer f2;

    @Field(position = 7)
    private Integer f7;

    @Field(position = 3)
    private Integer f3;

    @Field(position = 4)
    private Integer f4;

    @Field(position = 5)
    private Integer f5;

    @Field(position = 6)
    private Integer f6;

    public Integer getId() {
        return id;
    }

    public MockEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getF1() {
        return f1;
    }

    public MockEntity setF1(Integer f1) {
        this.f1 = f1;
        return this;
    }

    public Integer getF2() {
        return f2;
    }

    public MockEntity setF2(Integer f2) {
        this.f2 = f2;
        return this;
    }

    public Integer getF7() {
        return f7;
    }

    public MockEntity setF7(Integer f7) {
        this.f7 = f7;
        return this;
    }

    public Integer getF3() {
        return f3;
    }

    public MockEntity setF3(Integer f3) {
        this.f3 = f3;
        return this;
    }

    public Integer getF4() {
        return f4;
    }

    public MockEntity setF4(Integer f4) {
        this.f4 = f4;
        return this;
    }

    public Integer getF5() {
        return f5;
    }

    public MockEntity setF5(Integer f5) {
        this.f5 = f5;
        return this;
    }

    public Integer getF6() {
        return f6;
    }

    public MockEntity setF6(Integer f6) {
        this.f6 = f6;
        return this;
    }

    @Override
    public String toString() {
        return getId().toString()
                +" "+getF1().toString()
                +" "+getF2().toString()
                +" "+getF3().toString()
                +" "+getF4().toString()
                +" "+getF5().toString()
                +" "+getF6().toString()
                +" "+getF7().toString();
    }
}
