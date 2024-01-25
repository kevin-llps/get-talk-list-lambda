export const extract = (completion) => {
    return completion.split('```sql\n')[1].split('\n```\n')[0].replaceAll('\n', ' ');
};
