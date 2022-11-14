/**
 * Copyright (c) 2004-2022 Carnegie Mellon University and others. (see Contributors file).
 * All Rights Reserved.
 *
 * NO WARRANTY. ALL MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON UNIVERSITY MAKES NO WARRANTIES OF ANY
 * KIND, EITHER EXPRESSED OR IMPLIED, AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR PURPOSE
 * OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT
 * MAKE ANY WARRANTY OF ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT INFRINGEMENT.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * SPDX-License-Identifier: EPL-2.0
 *
 * Created, in part, with funding and support from the United States Government. (see Acknowledgments file).
 *
 * This program includes and/or can make use of certain third party source code, object code, documentation and other
 * files ("Third Party Software"). The Third Party Software that is used by this program is dependent upon your system
 * configuration. By using this program, You agree to comply with any and all relevant Third Party Software terms and
 * conditions contained in any such Third Party Software or separate license file distributed with such Third Party
 * Software. The parties who own the Third Party Software ("Third Party Licensors") are intended third party benefici-
 * aries to this license with respect to the terms applicable to their Third Party Software. Third Party Software li-
 * censes only apply to the Third Party Software and not any other portion of this program or this program as a whole.
 */

package org.osate.iso26262.fmea.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.osate.aadl2.ComponentImplementation;
import org.osate.aadl2.instance.ComponentInstance;
import org.osate.aadl2.instance.SystemInstance;
import org.osate.aadl2.instantiation.InstantiateModel;
import org.osate.iso26262.fmea.FmeaBuilder;
import org.osate.iso26262.fmea.export.FileExport;


public final class FMEAhandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

//		InstanceObject object = getTarget(HandlerUtil.getCurrentSelection(event));
//		if (object == null) {
//			Dialog.showInfo("Fault Tree Analysis", "Please choose an instance model");
//			return IStatus.ERROR;
//		}
//		SystemInstance si = object.getSystemInstance();
//		if (object instanceof ComponentInstance) {
//			target = (ComponentInstance) object;
//		} else {
//			target = si;
//		}
//


//		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		IOutlineNode node = (IOutlineNode) selection.getFirstElement();
		node.readOnly(state -> {
			SystemInstance rootInstance = null;
			EObject selectedObject = state;
			if (selectedObject instanceof SystemInstance) {
				rootInstance = (SystemInstance) selectedObject;
			}
			if (selectedObject instanceof ComponentImplementation) {
				try {
					rootInstance = InstantiateModel
							.buildInstanceModelFile((ComponentImplementation) selectedObject);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			ComponentInstance target = rootInstance;
			System.out.println("Start!!!!!");
			// 准备构建FMEA数据结构
			FmeaBuilder fb = new FmeaBuilder();
			// 构造结构树
			fb.Construct_structure_tree(target);
			// 同时构造故障网与功能网
			fb.BuildFailureAndFuncNet(fb.root_component);
			// 填充AP值
			fb.FillAP(fb.root_component);
			// 打印数据结构
			fb.Print_Structure(fb.root_component, "");

			// 从关注组件中获取表头
			fb.getHead();

			// 准备文件输出
			FileExport fe = new FileExport();
			fe.ExportFMEAreport(fb);
			System.out.println("FINISH!!AAAAAa");
			return Status.OK_STATUS;
		});
		return Status.error("error");
	}

//	private InstanceObject getTarget(ISelection currentSelection) {
//		if (currentSelection instanceof IStructuredSelection) {
//			IStructuredSelection iss = (IStructuredSelection) currentSelection;
//			if (iss.size() == 1) {
//				Object obj = iss.getFirstElement();
//				if (obj instanceof InstanceObject) {
//					return (InstanceObject) obj;
//				}
//				if (obj instanceof EObjectURIWrapper) {
//					EObject eObject = new ResourceSetImpl().getEObject(((EObjectURIWrapper) obj).getUri(), true);
//					if (eObject instanceof InstanceObject) {
//						return (InstanceObject) eObject;
//					}
//				}
//				if (obj instanceof IFile) {
//					URI uri = OsateResourceUtil.toResourceURI((IFile) obj);
//					Resource res = new ResourceSetImpl().getResource(uri, true);
//					EList<EObject> rl = res.getContents();
//					if (!rl.isEmpty()) {
//						return (InstanceObject) rl.get(0);
//					}
//				}
//			}
//		}
//		return null;
//	}


}
