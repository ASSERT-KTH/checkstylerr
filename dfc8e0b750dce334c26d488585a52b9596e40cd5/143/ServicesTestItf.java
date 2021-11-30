package servicesTest;

import groupservice.Person;
import es.bsc.compss.types.annotations.Parameter;
import es.bsc.compss.types.annotations.parameter.Direction;
import es.bsc.compss.types.annotations.task.Method;
import es.bsc.compss.types.annotations.task.Service;


public interface ServicesTestItf {
	
	@Method(declaringClass = "servicesTest.ServicesTestImpl")
	public void print (
		@Parameter(direction = Direction.IN) Person p
	);
	
	@Method(declaringClass = "servicesTest.ServicesTestImpl")
	public Person createPerson (
	);

	@Service(name = "GroupService", namespace = "http://groupService", port = "GroupServicePort")
	public int getNumWorkers(
	);

	@Service(name = "GroupService", namespace = "http://groupService", port = "GroupServicePort")
	public Person getOwner(
	);

	@Service(name = "GroupService", namespace = "http://groupService", port = "GroupServicePort")
	public Person getWorker(
		@Parameter(direction = Direction.IN)  int id
	);

	@Service(name = "GroupService", namespace = "http://groupService", port = "GroupServicePort")
	public double productivity(
	);

	@Service(name = "GroupService", namespace = "http://groupService", port = "GroupServicePort")
	public void setNumWorkers(
		@Parameter(direction = Direction.IN)  int numWorkers
	);

	@Service(name = "GroupService", namespace = "http://groupService", port = "GroupServicePort")
	public void setWorker(
		@Parameter(direction = Direction.IN)  Person worker, 
		@Parameter(direction = Direction.IN)  int id
	);

}
