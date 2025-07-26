package com.swiftgoal.app.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {
    
    // 球员名字中英文对照表
    private static final Map<String, String> PLAYER_NAME_TRANSLATIONS = new HashMap<>();
    
    static {
        // 添加一些常见球员的中文翻译
        // 英超球员
        PLAYER_NAME_TRANSLATIONS.put("Erling Haaland", "哈兰德");
        PLAYER_NAME_TRANSLATIONS.put("Kevin De Bruyne", "德布劳内");
        PLAYER_NAME_TRANSLATIONS.put("Mohamed Salah", "萨拉赫");
        PLAYER_NAME_TRANSLATIONS.put("Harry Kane", "哈里·凯恩");
        PLAYER_NAME_TRANSLATIONS.put("Bruno Fernandes", "布鲁诺·费尔南德斯");
        PLAYER_NAME_TRANSLATIONS.put("Marcus Rashford", "拉什福德");
        PLAYER_NAME_TRANSLATIONS.put("Phil Foden", "菲尔·福登");
        PLAYER_NAME_TRANSLATIONS.put("Bukayo Saka", "萨卡");
        PLAYER_NAME_TRANSLATIONS.put("Jude Bellingham", "贝林厄姆");
        PLAYER_NAME_TRANSLATIONS.put("Declan Rice", "赖斯");
        
        // 西甲球员
        PLAYER_NAME_TRANSLATIONS.put("Lionel Messi", "梅西");
        PLAYER_NAME_TRANSLATIONS.put("Cristiano Ronaldo", "C罗");
        PLAYER_NAME_TRANSLATIONS.put("Karim Benzema", "本泽马");
        PLAYER_NAME_TRANSLATIONS.put("Vinícius Júnior", "维尼修斯");
        PLAYER_NAME_TRANSLATIONS.put("Robert Lewandowski", "莱万多夫斯基");
        PLAYER_NAME_TRANSLATIONS.put("Luka Modrić", "莫德里奇");
        PLAYER_NAME_TRANSLATIONS.put("Toni Kroos", "克罗斯");
        PLAYER_NAME_TRANSLATIONS.put("Jude Bellingham", "贝林厄姆");
        PLAYER_NAME_TRANSLATIONS.put("Frenkie de Jong", "德容");
        PLAYER_NAME_TRANSLATIONS.put("Pedri", "佩德里");
        
        // 德甲球员
        PLAYER_NAME_TRANSLATIONS.put("Jamal Musiala", "穆西亚拉");
        PLAYER_NAME_TRANSLATIONS.put("Florian Wirtz", "维尔茨");
        PLAYER_NAME_TRANSLATIONS.put("Kai Havertz", "哈弗茨");
        PLAYER_NAME_TRANSLATIONS.put("Joshua Kimmich", "基米希");
        PLAYER_NAME_TRANSLATIONS.put("Thomas Müller", "托马斯·穆勒");
        PLAYER_NAME_TRANSLATIONS.put("Manuel Neuer", "诺伊尔");
        PLAYER_NAME_TRANSLATIONS.put("Alphonso Davies", "阿方索·戴维斯");
        PLAYER_NAME_TRANSLATIONS.put("Dayot Upamecano", "于帕梅卡诺");
        PLAYER_NAME_TRANSLATIONS.put("Matthijs de Ligt", "德里赫特");
        PLAYER_NAME_TRANSLATIONS.put("Kingsley Coman", "科曼");
        
        // 意甲球员
        PLAYER_NAME_TRANSLATIONS.put("Victor Osimhen", "奥斯梅恩");
        PLAYER_NAME_TRANSLATIONS.put("Khvicha Kvaratskhelia", "克瓦拉茨赫利亚");
        PLAYER_NAME_TRANSLATIONS.put("Lautaro Martínez", "劳塔罗·马丁内斯");
        PLAYER_NAME_TRANSLATIONS.put("Rafael Leão", "拉斐尔·莱奥");
        PLAYER_NAME_TRANSLATIONS.put("Theo Hernández", "特奥·埃尔南德斯");
        PLAYER_NAME_TRANSLATIONS.put("Mike Maignan", "迈尼昂");
        PLAYER_NAME_TRANSLATIONS.put("Sandro Tonali", "托纳利");
        PLAYER_NAME_TRANSLATIONS.put("Nicolò Barella", "巴雷拉");
        PLAYER_NAME_TRANSLATIONS.put("Federico Chiesa", "基耶萨");
        PLAYER_NAME_TRANSLATIONS.put("Dusan Vlahovic", "弗拉霍维奇");
        
        // 法甲球员
        PLAYER_NAME_TRANSLATIONS.put("Kylian Mbappé", "姆巴佩");
        PLAYER_NAME_TRANSLATIONS.put("Aurélien Tchouaméni", "楚阿梅尼");
        PLAYER_NAME_TRANSLATIONS.put("Eduardo Camavinga", "卡马文加");
        PLAYER_NAME_TRANSLATIONS.put("Warren Zaïre-Emery", "扎伊尔-埃梅里");
        PLAYER_NAME_TRANSLATIONS.put("Achraf Hakimi", "阿什拉夫");
        PLAYER_NAME_TRANSLATIONS.put("Marquinhos", "马尔基尼奥斯");
        PLAYER_NAME_TRANSLATIONS.put("Neymar", "内马尔");
        PLAYER_NAME_TRANSLATIONS.put("Marco Verratti", "维拉蒂");
        PLAYER_NAME_TRANSLATIONS.put("Gianluigi Donnarumma", "多纳鲁马");
        PLAYER_NAME_TRANSLATIONS.put("Presnel Kimpembe", "金彭贝");
        
        // 其他知名球员
        PLAYER_NAME_TRANSLATIONS.put("Jude Bellingham", "贝林厄姆");
        PLAYER_NAME_TRANSLATIONS.put("Eduardo Camavinga", "卡马文加");
        PLAYER_NAME_TRANSLATIONS.put("Aurélien Tchouaméni", "楚阿梅尼");
        PLAYER_NAME_TRANSLATIONS.put("Pedri", "佩德里");
        PLAYER_NAME_TRANSLATIONS.put("Gavi", "加维");
        PLAYER_NAME_TRANSLATIONS.put("Ansu Fati", "安苏·法蒂");
        PLAYER_NAME_TRANSLATIONS.put("Johan Bakayoko", "巴卡约科");
        PLAYER_NAME_TRANSLATIONS.put("Xavi Simons", "哈维·西蒙斯");
        PLAYER_NAME_TRANSLATIONS.put("Evan Ferguson", "埃文·弗格森");
        PLAYER_NAME_TRANSLATIONS.put("Cole Palmer", "科尔·帕尔默");
    }
    
    // 中文名字到英文名字的映射（用于搜索）
    private static final Map<String, String> CHINESE_TO_ENGLISH = new HashMap<>();
    
    static {
        // 为每个翻译创建反向映射
        for (Map.Entry<String, String> entry : PLAYER_NAME_TRANSLATIONS.entrySet()) {
            CHINESE_TO_ENGLISH.put(entry.getValue(), entry.getKey());
        }
    }
    
    /**
     * 将英文球员名字翻译为中文
     */
    public String translateToChinese(String englishName) {
        if (englishName == null || englishName.trim().isEmpty()) {
            return englishName;
        }
        
        String translated = PLAYER_NAME_TRANSLATIONS.get(englishName.trim());
        return translated != null ? translated : englishName;
    }
    
    /**
     * 将中文球员名字翻译为英文
     */
    public String translateToEnglish(String chineseName) {
        if (chineseName == null || chineseName.trim().isEmpty()) {
            return chineseName;
        }
        
        String translated = CHINESE_TO_ENGLISH.get(chineseName.trim());
        return translated != null ? translated : chineseName;
    }
    
    /**
     * 检查是否是中文名字
     */
    public boolean isChineseName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        // 检查是否包含中文字符
        return name.matches(".*[\\u4e00-\\u9fa5].*");
    }
    
    /**
     * 获取所有中文名字列表（用于搜索建议）
     */
    public java.util.List<String> getAllChineseNames() {
        return new java.util.ArrayList<>(CHINESE_TO_ENGLISH.keySet());
    }
    
    /**
     * 获取所有英文名字列表
     */
    public java.util.List<String> getAllEnglishNames() {
        return new java.util.ArrayList<>(PLAYER_NAME_TRANSLATIONS.keySet());
    }
} 