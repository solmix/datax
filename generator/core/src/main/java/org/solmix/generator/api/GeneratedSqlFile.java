package org.solmix.generator.api;


public class GeneratedSqlFile extends GeneratedFile
{
    /** The file name. */
    private String fileName;

    /** The target package. */
    private String targetPackage;
    private StringBuilder contentBuffer;


    public GeneratedSqlFile(String targetProject,String targetPackage,String fileName,StringBuilder contentBuffer)
    {
        super(targetProject);
        this.targetPackage=targetPackage;
        this.fileName=fileName;
        this.contentBuffer=contentBuffer;
    }

    @Override
    public String getFormattedContent() {
        return contentBuffer.toString();
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public String getTargetPackage() {
        return targetPackage;
    }

    @Override
    public boolean isMergeable() {
        return false;
    }

}
