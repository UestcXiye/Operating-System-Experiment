import java.util.Deque;
import java.util.LinkedList;

/**
 * @BelongsProject:OS-process
 * @BelongsPackage:PACKAGE_NAME
 * @Author:Uestc_Xiye
 * @CreateTime:2020-12-01 16:51:38
 */
public class Queue {
    /**
     * deque: 不同优先级就绪队列组成数组
     * readyQueue: 就绪队列
     */
    private Deque<Process>[] deque;
    private static final Queue readyQueue=new Queue();

    private Queue()
    {
        //因为进程有3种不同优先级，所以构造3个就绪队列
        deque=new LinkedList[3];
        for(int i=0;i<3;i++)
        {
            deque[i]=new LinkedList<>();
        }
    }

    public void addprocess(Process process)
    {
        int priority=process.getpriority();
        Deque<Process> d=deque[priority];
        d.addLast(process);
    }

    public boolean deleteProcess(Process process)
    {
        int priority = process.getpriority();
        Deque<Process> d=deque[priority];
        return d.remove(process);
    }

    public static Queue getreadyQueue()
    {
        return readyQueue;
    }

    public Process getprocess()
    {
        for (int i=2;i>=0;i--)
        {
            Deque<Process> d=deque[i];
            if(!d.isEmpty())
            {
                return d.peekFirst();
            }
        }
        return null;
    }
}
