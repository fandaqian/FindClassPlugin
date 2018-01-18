package com.fdq.findclassplugin;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by Fandaqian on 2018-01-18
 **/
public class FindClassAction extends AnAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindClassAction.class);
    private static final String FILE_SEP = System.getProperty("file.separator");
    private static final String OS_NAME = System.getProperty("os.name");

    private static final String SRC = "src";
    private static final String SMJ = SRC + FILE_SEP + "main" + FILE_SEP + "java";
    private static final String OUT = "out" + FILE_SEP + "production";
    private static final String TARGET = "target" + FILE_SEP + "classes";
    private static final String JAVA_EXT = ".java";
    private static final String CLASS_EXT = ".class";

    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        event.getPresentation().setVisible(true);
        event.getPresentation().setEnabled(editor != null);
        event.getPresentation().setIcon(AllIcons.General.Error);

        Project project = event.getData(PlatformDataKeys.PROJECT);
        DataContext dataContext = event.getDataContext();
        String ext = getFileExtension(dataContext);
        LOGGER.info(ext);
        if ("java".equals(ext)) {//根据扩展名判定是否进行下面的处理
            //获取选中的文件
            VirtualFile file = DataKeys.VIRTUAL_FILE.getData(event.getDataContext());
            if (null != project && file != null) {
                LOGGER.info(project.getName());
                LOGGER.info(String.valueOf(project.getBaseDir()));
                LOGGER.info(project.getBasePath());
                LOGGER.info(String.valueOf(project.getProjectFile()));
                LOGGER.info(project.getProjectFilePath());
                LOGGER.info(file.getPath());
                String path = file.getPath();

                if (path.contains(SMJ)) {
                    path = path.replaceAll(SMJ, TARGET);
                } else {
                    path = path.replaceAll(SRC, OUT + FILE_SEP + project.getName());
                }
                path = path.replaceAll(JAVA_EXT, CLASS_EXT);
                boolean fileExits = isFileExits(path);
                LOGGER.info(path + "===>" + fileExits);
                if (isFileExits(path)) {
                    String[] cmds = null;
                    if (isLinux()) {
                        cmds = new String[]{"nautilus", "--new-window", path};
                    }
                    if (null != cmds) {
                        runProcess(cmds);
                    }
                }
            }
        }
    }

    @Override
    public void update(AnActionEvent event) {
        //在Action显示之前,根据选中文件扩展名判定是否显示此Action
        String ext = getFileExtension(event.getDataContext());
        this.getTemplatePresentation().setEnabled(ext != null && "java".equals(ext));
    }

    private String getFileExtension(DataContext dataContext) {
        VirtualFile file = DataKeys.VIRTUAL_FILE.getData(dataContext);
        return file == null ? null : file.getExtension();
    }

    private boolean isLinux() {
        return OS_NAME.toLowerCase().contains("linux") || OS_NAME.toLowerCase().contains("mac os x");
    }

    private void runProcess(String[] cmds) {
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);
        Process p;
        BufferedReader br = null;
        try {
            p = pb.start();
            String line;

            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != br) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isFileExits(String path) {
        if (isLinux()) {
            return new File(path).exists();
        } else {
            try {
                return new File(new URI(path)).exists();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
