package com.github.btpka3.lucene.analysis;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.tokenattributes.*;
import org.apache.lucene.util.CharsRef;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;


public class PinyinAbbrTokenFilter extends TokenFilter {

    private SynonymMap pinyinSynonymMap;
    private HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
    private SynonymFilter synonymFilter;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final PositionLengthAttribute posLenAtt = addAttribute(PositionLengthAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private void setupSynonymMap()
            throws IOException, BadHanyuPinyinOutputFormatCombination {

        char[][] chineseChars = {
                // {from, to}
                {'\u4e00', '\u9fa5'}
        };

        SynonymMap.Builder builder = new SynonymMap.Builder(true);

        for (int i = 0; i < chineseChars.length; i++) {
            char[] charRange = chineseChars[i];
            for (char c = charRange[0]; c <= charRange[1]; c++) {
                String[] pinyinArr = pinyinSynonymArr(c);
                if (pinyinArr == null) {
                    continue;
                }
                addTo(builder,
                        new String[]{Character.toString(c)},
                        flattenPinyinArr(pinyinArr));
            }
        }
        pinyinSynonymMap = builder.build();
    }

    private String[] pinyinSynonymArr(char c)
            throws BadHanyuPinyinOutputFormatCombination {
        String[] pinyinArr = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
        if (pinyinArr == null) {
            return null;
        }

        String[] flattenPinyinArr = flattenPinyinArr(pinyinArr);
        String[] withOrgCharPinyinArr = new String[flattenPinyinArr.length + 1];
        withOrgCharPinyinArr[0] = Character.toString(c);
        System.arraycopy(flattenPinyinArr, 0, withOrgCharPinyinArr, 1, flattenPinyinArr.length);
        return withOrgCharPinyinArr;
    }

    // 称 {chèn, chēng } -> {c,ch,che,cheng}
    // 重 {zhòng,chóng} -> {z,zh,zho,zhon,zhong, c,ch,cho,chon,chong}
    private String[] flattenPinyinArr(String[] pinyinArr) {
        if (pinyinArr == null) {
            return null;
        }
        Set<String> pinyinAbbrSet = new TreeSet<String>();
        for (int i = 0; i < pinyinArr.length; i++) {
            String pinyin = pinyinArr[i];
            for (int j = 1; j <= pinyin.length(); j++) {
                pinyinAbbrSet.add(pinyin.substring(0, 0 + j));
            }
        }

        return pinyinAbbrSet.toArray(new String[0]);
    }

    private void addTo(SynonymMap.Builder builder, String[] from, String[] to) {
        for (String input : from) {
            for (String output : to) {
                builder.add(new CharsRef(input), new CharsRef(output), false);
            }
        }
    }

    public static boolean isChineseChar(char c) {
        return '\u4e00' <= c && c <= '\u9fa5';
    }

    public static boolean containsChineseChar(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (isChineseChar(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    public PinyinAbbrTokenFilter(TokenStream in) {
        super(in);
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);

        try {
            setupSynonymMap();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        synonymFilter = new SynonymFilter(input, pinyinSynonymMap, true);
    }

    private HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
    private String padding_char;
    private String first_letter;

    @Override
    public final boolean incrementToken() throws IOException {

        if (!synonymFilter.incrementToken()) {
            return false;
        }

        CharTermAttribute termAtt0 = synonymFilter.getAttribute(CharTermAttribute.class);
        PositionIncrementAttribute posIncrAtt0 = synonymFilter.getAttribute(PositionIncrementAttribute.class);
        PositionLengthAttribute posLenAtt0 = synonymFilter.getAttribute(PositionLengthAttribute.class);
        TypeAttribute typeAtt0 = synonymFilter.getAttribute(TypeAttribute.class);
        OffsetAttribute offsetAtt0 = synonymFilter.getAttribute(OffsetAttribute.class);
        return true;
    }

    @Override
    public final void end() throws IOException {
        // set final offset
        super.end();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
    }

}
