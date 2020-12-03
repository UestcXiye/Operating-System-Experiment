import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @BelongsProject:OS-process
 * @BelongsPackage:PACKAGE_NAME
 * @Author:Uestc_Xiye
 * @CreateTime:2020-12-01 16:52:02
 */

public class Process {
    /**
     * 变量说明
     * pid: 进程的id，唯一
     * pName: 进程的名字
     * priority: 进程的优先级
     * state: 进程的状态，有五种，具体见于State中
     * blockedResource: 如果进程状态为阻塞的话，这个属性指向被阻塞的资源，否则为NULL
     * resourceMap: 进程持有的资源和相应数量
     * parent: 进程的父进程
     * children: 进程的子进程（们）
     */
    private int pid;
    private String pName;
    private int priority;
    private State state;
    private Resource blockedResource;
    private ConcurrentHashMap<Resource,Integer> resourceMap;
    private Process parent;
    private List<Process> children;

    private static final Pcb pcb=Pcb.getpcb();
    private static final Queue readyQueue=Queue.getreadyQueue();

    /**
     * 进程的五种状态
     * NEW: 新建状态
     * READY: 就绪状态
     * RUNNING: 执行状态
     * BLOCKED: 阻塞状态
     * TERMINATED: 终止状态
     */
    public enum State
    {
        NEW,READY,RUNNING,BLOCKED,TERMINATED
    }

    public Process(int pid,String pName,int priority,State state,ConcurrentHashMap<Resource,Integer> resourceMap,Process parent,List<Process> children)
    {
        this.pid=pid;
        this.pName=pName;
        this.priority=priority;
        this.state=state;
        this.resourceMap=resourceMap;
        this.parent=parent;
        this.children=children;
    }

    public void deleteChild(Process process)
    {
        for(Process child:children)
        {
            if(child==process)
            {
                children.remove(child);
                return;
            }
        }
    }

    public void deleteProcessTree()
    {
        if(!children.isEmpty())
        {
            for(int i=0;i<children.size();i++)
            {
                Process child=children.get(0);
                child.deleteProcessTree();// 递归删除子树
            }
        }
        // 对不同状态的进程处理

        // 若进程状态为终止状态，说明删除成功
        if(this.getstate()==State.TERMINATED)
        {
            pcb.deleteProcess(this);
            return;
        }
        // 若进程状态为就绪状态，则从就绪队列删除，修改其状态为终止状态
        else if(this.getstate()==State.READY)
        {
            readyQueue.deleteProcess(this);
            pcb.deleteProcess(this);
            this.setstate(State.TERMINATED);
        }
        // 若进程状态为阻塞状态，则从阻塞队列删除，修改其状态为终止状态
        else if(this.getstate()==State.BLOCKED)
        {
            Resource blockedResource=this.getblockedResource();
            blockedResource.deleteblockedProcess(this);
            pcb.deleteProcess(this);
            this.setstate(State.TERMINATED);
        }
        // 若进程状态为运行状态时直接终止，则修改其状态为终止状态
        else if(this.getstate()==State.RUNNING)
        {
            pcb.deleteProcess(this);
            this.setstate(State.TERMINATED);
        }
        // 清除进程的parent和child指针
        parent.deleteChild(this);
        parent=null;
        // 释放资源
        for(Resource resource:resourceMap.keySet())
        {
            resource.releaseResource(this);
        }
        return;
    }

    public void destroy()
    {
        deleteProcessTree();
        Pcb.scheduler();
        return;
    }

    public void setpid(int pid)
    {
        this.pid=pid;
    }

    public void setpName(String pName)
    {
        this.pName=pName;
    }

    public void setpriority(int priority)
    {
        this.priority=priority;
    }

    public void setstate(State state)
    {
        this.state=state;
    }

    public void setblockedResource(Resource blockedResource)
    {
        this.blockedResource=blockedResource;
    }

    public void setrMap(ConcurrentHashMap<Resource,Integer> resourceMap)
    {
        this.resourceMap=resourceMap;
    }

    public void setparent(Process parent)
    {
        this.parent=parent;
    }

    public void setchildren(List<Process> children)
    {
        this.children=children;
    }

    public int getpid()
    {
        return pid;
    }

    public String getpName()
    {
        return pName;
    }

    public int getpriority()
    {
        return priority;
    }

    public State getstate()
    {
        return state;
    }

    public Resource getblockedResource()
    {
        return blockedResource;
    }

    public ConcurrentHashMap<Resource,Integer> getresourceMap()
    {
        return resourceMap;
    }

    public Process getparent()
    {
        return parent;
    }

    public List<Process> getchildren()
    {
        return children;
    }

}
