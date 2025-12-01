package academy.cli.converter;

import academy.enums.ReportFileType;
import picocli.CommandLine.ITypeConverter;

public class ReportFileTypeConverter implements ITypeConverter<ReportFileType> {

    @Override
    public ReportFileType convert(String value) throws Exception {
        return ReportFileType.fromString(value);
    }
}
