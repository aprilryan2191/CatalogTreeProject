package aprilryan.tree.entity;

import javax.persistence.*;

@Entity
@Table(name = "tree")
public class Tree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "left_key")
    private Integer leftKey;

    @Column(name = "right_key")
    private Integer rightKey;

    private Integer level;

    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getLeftKey() {
        return leftKey;
    }

    public void setLeftKey(Integer leftKey) {
        this.leftKey = leftKey;
    }

    public Integer getRightKey() {
        return rightKey;
    }

    public void setRightKey(Integer rightKey) {
        this.rightKey = rightKey;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

