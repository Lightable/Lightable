export default function(bytes: number): `${number} ${'B' | 'KB' | 'MB' | 'GB' | 'TB'}` {
    const index = Math.floor(Math.log(bytes) / Math.log(1024));
    return `${Number((bytes / Math.pow(1024, index)).toFixed(2)) * 1} ${(['B', 'KB', 'MB', 'GB', 'TB'] as const)[index]}`;
};