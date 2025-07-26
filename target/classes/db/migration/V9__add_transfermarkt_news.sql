-- Flyway migration script for adding demo news from Transfermarkt

-- Article 1: Sang-Bin Jeong joins St. Louis CITY SC
INSERT INTO news_articles (source, publish_date, title, title_cn, summary_ai, summary_ai_cn, raw_html_content, user_generated, upload_time, moderation_status) VALUES
(
    'Transfermarkt',
    NOW(),
    'Third biggest signing in history - Sang-Bin Jeong joins St. Louis CITY SC from Minnesota United',
    '历史第三高签约费 - 郑想宾从明尼苏达联加盟圣路易斯城',
    'St. Louis CITY SC have acquired South Korean U22 Initiative forward Sang-Bin Jeong from Minnesota United in exchange for $2m in General Allocation Money, making it a significant trade within the MLS.',
    '圣路易斯城足球俱乐部通过200万美元的通用分配资金，从明尼苏达联队引进了韩国U22创举计划前锋郑想宾，这是美国职业足球大联盟内部一笔重要的交易。',
    '<div class="article-content">
        <p>圣路易斯城足球俱乐部从明尼苏达联队获得了韩国U22创举计划前锋郑想宾。作为回报，明尼苏达联队将获得200万美元的通用分配资金（GAM）。其中150万美元将在2025年支付，50万美元将在2026年支付。如果满足某些条件，明尼苏达联队还可以额外获得30万美元的GAM，并且还将保留郑想宾未来转会费的20%分成。对于圣路易斯城来说，这是俱乐部历史上第三大引援费用。</p>
        <img src="/images/news/sang-bien-jeong-1753136466-172844.jpg" alt="Sang-Bin Jeong St. Louis CITY SC" style="width:100%; max-width:700px; border-radius:8px; margin: 1rem 0;">
        <p>“我们很高兴能将想宾这样一位充满活力和前途的年轻球员加入到我们的阵容中，”圣路易斯城体育总监卢茨·范嫩施蒂尔在一份俱乐部声明中说道。“他在对方半场展现出的品质正是我们希望为球队增添的，我们相信他能很好地融入我们的比赛风格。我们期待着帮助他继续在他的职业生涯中发展。”</p>
        <p>23岁的郑想宾最初在2023年从狼队加盟明尼苏达联队。这位前锋在U22创举计划名单上，并在为明尼苏达出场的51场比赛中（所有比赛）贡献了13个进球和5次助攻。作为一名多才多艺的前锋，郑想宾可以胜任前场的任何位置。但是，这位韩国前锋在本赛季大部分时间里都在努力争取常规首发时间，并且在球队主教练埃里克·拉姆齐的带领下，似乎已经不再是球队计划的一部分。</p>
    </div>',
    false, NOW(), 1
);

-- Article 2: Why Xavi Simons is ready to move on from RB Leipzig this summer
INSERT INTO news_articles (source, publish_date, title, title_cn, summary_ai, summary_ai_cn, raw_html_content, user_generated, upload_time, moderation_status) VALUES
(
    'Transfermarkt',
    NOW(),
    'Wanted by Chelsea - Why Xavi Simons is ready to move on from RB Leipzig this summer',
    '切尔西的目标 - 为何哈维·西蒙斯准备好在今年夏天离开RB莱比锡',
    'After a stellar season on loan at RB Leipzig, Xavi Simons is one of the hottest properties on the summer transfer market. With PSG looking to sell, clubs like Chelsea, Barcelona, and Bayern Munich are all in the race for the talented Dutch playmaker.',
    '在租借至RB莱比锡度过一个辉煌的赛季后，哈维·西蒙斯已成为夏季转会市场上最炙手可热的球员之一。由于巴黎圣日耳曼希望出售他，切尔西、巴塞罗那和拜仁慕尼黑等俱乐部都在争夺这位才华横溢的荷兰组织核心。',
    '<div class="article-content">
        <p>在租借至RB莱比锡度过一个出色的赛季后，哈维·西蒙斯是夏季转会市场上最受关注的球员之一。这位22岁的球员目前正随荷兰队参加欧洲杯，但他似乎肯定会在今年夏天离开巴黎圣日耳曼。据《图片报》报道，切尔西已经加入了对这位荷兰国脚的争夺战。但蓝军面临着来自巴塞罗那和拜仁慕尼黑的激烈竞争。</p>
        <img src="/images/news/xavi-simons-to-chelsea-1753134953-172843.png" alt="Xavi Simons RB Leipzig 2023/24" style="width:100%; max-width:700px; border-radius:8px; margin: 1rem 0;">
        <p>这位荷兰组织核心在所有比赛中为莱比锡出场43次，打进10球并有15次助攻，之后他的未来变得不确定。莱比锡很想留住他，但这家德甲俱乐部更倾向于再次租借，而巴黎圣日耳曼则希望直接出售。据信，巴黎圣日耳曼希望获得约7000万欧元（5920万英镑）的转会费。这对于莱比锡来说太高了，但对于像切尔西、巴塞罗那和拜仁这样的俱乐部来说是可以承受的。</p>
        <p>那么，为什么西蒙斯在莱比锡度过一个成功的赛季后还想离开呢？答案很简单。西蒙斯希望在一个能够保证他获得欧冠联赛资格的俱乐部踢球。尽管莱比锡是一支顶级球队，但他们获得前四名的位置并不总是板上钉钉。另一方面，切尔西、巴塞罗那和拜仁都是各自联赛的常年竞争者。他们也更有可能挑战欧冠冠军，这是西蒙斯的另一个职业目标。</p>
    </div>',
    false, NOW(), 1
);

-- Article 3: Kenji Cabrera joins the Vancouver Whitecaps
INSERT INTO news_articles (source, publish_date, title, title_cn, summary_ai, summary_ai_cn, raw_html_content, user_generated, upload_time, moderation_status) VALUES
(
    'Transfermarkt',
    NOW(),
    'Kenji Cabrera joins the Vancouver Whitecaps - Pedro Vite replacement',
    '肯吉·卡布雷拉加盟温哥华白浪 - 作为佩德罗·维特的替代者',
    'The Vancouver Whitecaps have signed 22-year-old Peruvian winger Kenji Cabrera from FBC Melgar as a replacement for Pedro Vite. The deal is worth $1.2m and Cabrera has signed a contract until 2028.',
    '温哥华白浪队从FBC梅尔加签下了22岁的秘鲁边锋肯吉·卡布雷拉，以替代佩德罗·维特。这笔交易价值120万美元，卡布雷拉签订了一份直到2028年的合同。',
    '<div class="article-content">
        <p>温哥华白浪队从FBC梅尔加签下了肯吉·卡布雷拉。两家俱乐部现已正式宣布这笔交易。这位22岁的秘鲁边锋被认为是佩德罗·维特的替代者。这位秘鲁球员有解约条款，费用为120万美元（100万欧元）。个人条款已于上周达成一致，卡布雷拉签订了一份到2028年的合同，俱乐部有2029年的续约选项，他将成为MLS U22创举计划的一员。</p>
        <img src="/images/news/done-deal-kenji-cabrera-to-vancouver-whitecaps-1753155519-172846.png" alt="Kenji Cabrera FBC Melgar" style="width:100%; max-width:700px; border-radius:8px; margin: 1rem 0;">
        <p>温哥华体育总监阿克塞尔·舒斯特已计划将维特转会费的一部分资金立即用于加强阵容。卡布雷拉被认为是一位具有巨大潜力且能立即帮助球队的球员。卡布雷拉出生于日本滋贺，是秘鲁一颗冉冉升起的新星。本赛季，他在各项赛事的26场比赛中攻入9球并有4次助攻。因此，自去年11月以来，卡布雷拉的市场价值翻了一番，从80万欧元增长到现在的160万欧元。</p>
        <p>在秘鲁，卡布雷拉被视为一颗冉冉升起的新星。“卡布雷拉代表了秘鲁国家队为迎接未来挑战所需的新面孔之一，”Transfermarkt秘鲁地区经理格尔森·罗梅罗说。“由于他出色的盘带能力以及在进球和助攻方面的进攻影响力，他是秘鲁最有才华的U-23前锋。他在秘鲁联赛和南美解放者杯上的出色表现使他成为一名可靠且富有成效的球员。”</p>
    </div>',
    false, NOW(), 1
); 