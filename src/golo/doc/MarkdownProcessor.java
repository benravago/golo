package golo.doc;

import java.nio.file.Path;
import java.util.Collection;

import golo.lang.FunctionReference;
import golo.lang.IO;

public class MarkdownProcessor extends AbstractProcessor {

  @Override
  protected String fileExtension() {
    return "markdown";
  }

  @Override
  public String render(ModuleDocumentation documentation) throws Throwable {
    FunctionReference template = template("template", fileExtension());
    addModule(documentation);
    return (String) template.invoke(documentation);
  }

  @Override
  public void process(Collection<ModuleDocumentation> modules, Path targetFolder) throws Throwable {
    setTargetFolder(targetFolder);
    for (ModuleDocumentation doc : modules) {
      IO.textToFile(render(doc), outputFile(doc.moduleName()));
    }
    renderIndex("index");
  }
}
