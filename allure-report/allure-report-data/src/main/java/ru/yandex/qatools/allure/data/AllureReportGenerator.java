package ru.yandex.qatools.allure.data;

import org.apache.commons.io.FileUtils;
import ru.yandex.qatools.allure.data.providers.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.yandex.qatools.allure.data.utils.AllureReportUtils.listFiles;


/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 31.10.13
 */
public class AllureReportGenerator {

    private static final String ATTACHMENTS_MASK = ".+-attachment\\.\\w+";

    private List<DataProvider> dataProviders = new ArrayList<>();

    protected File[] inputDirectories;

    public AllureReportGenerator(File... inputDirectories) {
        this.inputDirectories = inputDirectories;
        registerDataProviders(defaultProviders());
    }

    public void generate(File outputDirectory) {
        copyAttachments(inputDirectories, outputDirectory);

        String testRun = new TestSuiteFiles(inputDirectories).generateTestRun();

        for (DataProvider provider : dataProviders) {
            provider.provide(testRun, outputDirectory);
        }

    }

    public void registerDataProviders(DataProvider... ts) {
        Collections.addAll(dataProviders, ts);
    }

    public static DataProvider[] defaultProviders() {
        return new DataProvider[]{
                new XUnitDataProvider(),
                new GraphDataProvider(),
                new TestCasesDataProvider(),
                new BehaviorDataProvider()
        };
    }

    public static File[] getAttachmentsFiles(File... dirs) {
        return listFiles(dirs, ATTACHMENTS_MASK);
    }

    public static void copyAttachments(File[] dirs, File outputDirectory) {
        for (File attach : getAttachmentsFiles(dirs)) {
            try {
                copyAttachment(attach, new File(outputDirectory, attach.getName()));
            } catch (IOException e) {
                throw new ReportGenerationException(e);
            }
        }
    }

    public static void copyAttachment(File srcFile, File destFile) throws IOException {
        if (!srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
            FileUtils.copyFile(srcFile, destFile);
        }
    }
}
