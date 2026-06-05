/**
 * 活动类别映射
 */
const categoryMap = {
    academic: '学术科技',
    sports: '体育竞技',
    cultural: '文化艺术',
    volunteer: '志愿服务',
    other: '其他',
};
export function categoryLabel(cat) {
    return categoryMap[cat] || cat || '未分类';
}
/**
 * 分数等级映射
 */
export function previewLevel(score) {
    if (score >= 90)
        return '优秀';
    if (score >= 80)
        return '良好';
    if (score >= 70)
        return '中等';
    if (score >= 60)
        return '及格';
    return '不及格';
}
export function levelTagType(score) {
    if (score >= 90)
        return 'success';
    if (score >= 80)
        return 'primary';
    if (score >= 70)
        return 'warning';
    if (score >= 60)
        return 'warning';
    return 'danger';
}
export function levelTagTypeForLevel(level) {
    switch (level) {
        case '优': return 'success';
        case '良': return 'primary';
        case '中': return 'warning';
        case '及格': return 'warning';
        default: return 'danger';
    }
}
