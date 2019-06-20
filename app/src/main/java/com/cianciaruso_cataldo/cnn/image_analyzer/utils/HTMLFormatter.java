package com.cianciaruso_cataldo.cnn.image_analyzer.utils;

import org.apache.commons.lang3.StringUtils;

public class HTMLFormatter {
        public enum HTMLColor{red,white,blue,yellow}

        public static String getColoredText(String text, HTMLColor color ){
            return "<font color=\""+color+"\">"+text+"</font>";
        }

        public static String getWhiteSpaces(int num){
            return StringUtils.repeat("&nbsp;"," ",num);
        }
}
