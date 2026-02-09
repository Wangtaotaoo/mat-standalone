/*******************************************************************************
 * Standalone console progress listener for MAT.
 *******************************************************************************/
package org.eclipse.mat.util;

/**
 * Simple IProgressListener that prints progress to System.out.
 */
public class ConsoleProgressListener implements IProgressListener
{
    private boolean canceled = false;
    private String taskName;
    private int totalWork;
    private int worked;

    public void beginTask(String name, int totalWork)
    {
        this.taskName = name;
        this.totalWork = totalWork;
        this.worked = 0;
        System.out.println("[MAT] " + name + " (total work: " + totalWork + ")");
    }

    public void done()
    {
        if (taskName != null)
            System.out.println("[MAT] " + taskName + " done.");
    }

    public boolean isCanceled()
    {
        return canceled;
    }

    public void setCanceled(boolean value)
    {
        this.canceled = value;
    }

    public void subTask(String name)
    {
        System.out.println("[MAT]   " + name);
    }

    public void worked(int work)
    {
        this.worked += work;
    }

    public void sendUserMessage(Severity severity, String message, Throwable exception)
    {
        String prefix;
        switch (severity)
        {
            case ERROR:
                prefix = "[MAT ERROR] ";
                break;
            case WARNING:
                prefix = "[MAT WARN]  ";
                break;
            default:
                prefix = "[MAT INFO]  ";
                break;
        }
        System.out.println(prefix + message);
        if (exception != null && severity == Severity.ERROR)
        {
            exception.printStackTrace(System.err);
        }
    }
}
