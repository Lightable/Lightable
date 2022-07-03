export type AttachmentID = string;
export type AttachmentMeta = (
    { type: 'File' } |
    { type: 'Text' } |
    { type: 'Audio' } |
    { type: 'Image', width: number, height: number } |
    { type: 'Video', width: number, height: number }
);
export type AttachmentTag = 'attachments' | 'avatars' | 'backgrounds' | 'icons';
export type Attachment = {
    _id: AttachmentID

    tag: AttachmentTag

    /**
     * File size (in bytes)
     */
    size: number

    /**
     * File name
     */
    filename: string

    /**
     * Metadata
     */
    metadata: AttachmentMeta

    /**
     * Content type
     */
    content_type: string
}
export interface SizeOptions {
    /**
     * Width of resized image
     */
    width?: number

    /**
     * Height of resized image
     */
    height?: number

    /**
     * Width and height of resized image
     */
    size?: number

    /**
     * Maximum resized image side length
     */
    max_side?: number
}


