/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.gary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.gary.GraphConstraint;
import solver.constraints.gary.relations.GraphRelation;
import solver.constraints.propagators.GraphPropagator;
import solver.exception.ContradictionException;
import solver.requests.GraphRequest;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.Variable;
import solver.variables.domain.delta.IntDelta;
import solver.variables.graph.GraphVar;

/**Propagator channeling a graph and an array of integer variables
 * 
 * @author Jean-Guillaume Fages
 */
public class PropRelation<V extends Variable, G extends GraphVar> extends GraphPropagator<V> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private G g;
	private int n;
	public static long duration;
	private Variable[] intVars;
	private IntProcedure arcEnforced;
	private IntProcedure arcRemoved;
	private Solver solver;
	private GraphRelation relation;

	//***********************************************************************************
	// CONSTRUCTOR
	//***********************************************************************************

	public PropRelation(Variable[] vars, G graph,Solver solver, GraphConstraint cons,GraphRelation relation) {
		super((V[]) ArrayUtils.append(vars,new Variable[]{graph}), solver, cons, relation.getPriority(), true);
		this.g = graph;
		this.intVars = vars;
		this.n = intVars.length;
		this.relation = relation;
		this.solver = solver;
		this.arcEnforced = new EdgeEnf();
		this.arcRemoved = new EdgeRem();
		duration = 0;
	}

	//***********************************************************************************
	// PROPAGATIONS
	//***********************************************************************************

	@Override
	public void propagate() throws ContradictionException {
		long time = System.currentTimeMillis();
		for(int i=0; i<n; i++){
			for(int j=0; j<n; j++){
				if(g.getKernelGraph().arcExists(i, j)){
					apply(i, j);
				}else{
					if(g.getEnvelopGraph().arcExists(i, j)){
						switch(relation.isEntail(i,j)){
						case TRUE: g.enforceArc(i, j, null);break;
						case FALSE: g.removeArc(i, j, null);break;
						}
					}else{
						unapply(i, j);
					}
				}
			}
		}
		duration += System.currentTimeMillis()-time;
	}

	@Override
	public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
		long time = System.currentTimeMillis();
		if (request instanceof GraphRequest) {
			GraphRequest gr = (GraphRequest) request;
			if((mask & EventType.ENFORCEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcEnforcingDelta();
				d.forEach(arcEnforced, gr.fromArcEnforcing(), gr.toArcEnforcing());
			}
			if((mask & EventType.REMOVEARC.mask) !=0){
				IntDelta d = (IntDelta) g.getDelta().getArcRemovalDelta();
				d.forEach(arcRemoved, gr.fromArcRemoval(), gr.toArcRemoval());
			}
		}
		else{
			checkVar(idxVarInProp);
		}
		duration += System.currentTimeMillis()-time;
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.ALL_MASK() + EventType.ENFORCEARC.mask + EventType.REMOVEARC.mask + EventType.META.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.UNDEFINED;
	}

	//***********************************************************************************
	// PROCEDURES
	//***********************************************************************************

	private void apply(int x, int y) throws ContradictionException{
		relation.applyTrue(x,y, solver, null);
	}

	private void unapply(int x, int y) throws ContradictionException{
		relation.applyFalse(x,y, solver, null);
	}

	private void checkVar(int i) throws ContradictionException {
		for(int j=0; j<n; j++){
			if(g.getKernelGraph().arcExists(i, j)){
				apply(i, j);
			}else{
				if(g.getEnvelopGraph().arcExists(i, j)){
					switch(relation.isEntail(i,j)){
					case TRUE: g.enforceArc(i, j, null);break;
					case FALSE: g.removeArc(i, j, null);break;
					}
				}else{
					unapply(i, j);
				}
			}
		}
	}

	/** When an edge (x,y), is enforced then the relation xRy must be true */
	private class EdgeEnf implements IntProcedure {
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			apply(from, to);
		}
	}
	/** When an edge (x,y), is removed then the non relation x!Ry must be true iff both x and y are in the kernel */
	private class EdgeRem implements IntProcedure {
		@Override
		public void execute(int i) throws ContradictionException {
			int from = i/n-1;
			int to   = i%n;
			if(g.getKernelGraph().getActiveNodes().isActive(from) && g.getKernelGraph().getActiveNodes().isActive(to)){
				unapply(from, to);
			}
		}
	}
}