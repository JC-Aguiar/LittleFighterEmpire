package br.com.jcaguiar.lfe.components.objects.structure;

import lombok.ToString;
import lombok.val;

@ToString
public class DataBmp {
    public int startIndex, endIndex;
    public String path;
    public int w, h, row, col;

    public DataBmp(String fileIndexes, String filePath, String w, String h, String row, String col) {
        val indexes = fileIndexes.replace("file(", "")
            .replace(")", "")
            .split("-");
        this.startIndex = Integer.parseInt(indexes[0]);
        this.endIndex = Integer.parseInt(indexes[1]);
        this.path = filePath;
        this.w = Integer.parseInt(w);
        this.h = Integer.parseInt(h);
        this.row = Integer.parseInt(row);
        this.col = Integer.parseInt(col);
        System.out.println("Load Data Files: new <bmp> object: " + this);
    }

}
