
package org.solmix.generator.codegen.datax;

import static org.solmix.generator.api.OutputUtilities.newLine;

import java.util.ArrayList;
import java.util.List;

import org.solmix.commons.util.StringUtils;
import org.solmix.generator.api.GeneratedSqlFile;
import org.solmix.generator.api.ProgressCallback;
import org.solmix.generator.config.ColumnInfo;
import org.solmix.generator.config.DomainInfo;
import org.solmix.generator.config.SqlGeneratorInfo;
import org.solmix.generator.config.TableInfo;

public class SqlCreateGenerator
{

    protected DomainInfo domain;

    protected List<String> warnings;

    protected ProgressCallback progressCallback;

    public SqlCreateGenerator()
    {
        super();
    }

    public DomainInfo getDomain() {
        return domain;
    }

    public void setDomain(DomainInfo domain) {
        this.domain = domain;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public ProgressCallback getProgressCallback() {
        return progressCallback;
    }

    public void setProgressCallback(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    public GeneratedSqlFile generate() {
        SqlGeneratorInfo sgi = domain.getSqlGeneratorInfo();
        String fileName = "create." + domain.getId() + ".sql";
        StringBuilder sb = new StringBuilder();
        sb.append("/* Domain:[" + domain.getId() + "] ");
        newLine(sb);
        sb.append(" * This Sql Script was generated by Code Generator. ");
        newLine(sb);
        sb.append(" */");
        newLine(sb);
        List<TableInfo> tables = domain.getTableInfos();
        for (TableInfo ti : tables) {
            String tableName = ti.getTableName();
            String remark = ti.getRemark();
            sb.append("/* Table :[" + tableName + "] ,Name:" + remark + "*/");
            newLine(sb);
            sb.append("create table ").append(tableName).append("\n");
            sb.append("(\n");
            int maxNameLength = 30;
            List<ColumnInfo> columns = ti.getColumns();
            List<String> pks = new ArrayList<String>();
            for (int cc = 0; cc < columns.size(); cc++) {
                ColumnInfo c = columns.get(cc);
                String column = c.getColumn();
                if (StringUtils.isEmpty(column)) {
                    continue;
                }
                int spaces = maxNameLength - column.length() + 5;
                sb.append("  ");
                sb.append(column);
                sb.append(getSpace(spaces));
                String type = c.getNativeType();

                sb.append(type);
                if (c.isPrimaryKey()) {
                    sb.append(" NOT NULL");
                    pks.add(column);
                } else if (!c.isNullable()) {
                    sb.append(" NOT NULL");
                }
                if (c.getRemark() != null) {
                    sb.append(" COMMENT '");
                    sb.append(c.getRemark()).append("'");
                }
                if(cc<columns.size()-1)
                    sb.append(",\n");
            }
            if(pks.size()>0){
                sb.append(",\n");
                sb.append("  primary key (");
                for (int p = 0; p < pks.size(); p++) {
                    if (p > 0) {
                        sb.append(",");
                    }
                    sb.append(pks.get(p));
                }
                sb.append(")\n");
            }else{
                sb.append("\n");
            }
            sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;\n");
        }
        GeneratedSqlFile file = new GeneratedSqlFile(sgi.getTargetProject(), sgi.getTargetPackage()+".create", fileName, sb);
        return file;

    }

    private static String getSpace(int spaces) {
        if (spaces <= 0) {
            spaces = 10;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
