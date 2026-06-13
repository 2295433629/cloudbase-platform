package com.cloudbase.common.core.domain;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Tree 基类（参考 RuoYi TreeSelect）
 *
 * @author ruoyi
 */
public class TreeSelect implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String label;

    /** 子节点 */
    private List<TreeSelect> children = new LinkedList<>();

    public TreeSelect() {}

    public TreeSelect(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    // getters and setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public List<TreeSelect> getChildren() { return children; }
    public void setChildren(List<TreeSelect> children) { this.children = children; }
}
