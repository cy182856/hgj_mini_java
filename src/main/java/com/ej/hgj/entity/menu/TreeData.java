package com.ej.hgj.entity.menu;

import lombok.Data;

import java.util.List;

@Data
public class TreeData {

    private String id;

    private String label;

    private List<Children> children;

}
