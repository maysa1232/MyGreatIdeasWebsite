package com.mays.GreatIdeas.Controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mays.GreatIdeas.Models.Idea;
import com.mays.GreatIdeas.Models.User;
import com.mays.GreatIdeas.Repository.IdeaRepository;
import com.mays.GreatIdeas.Repository.UserRepository;
import com.mays.GreatIdeas.Service.IdeaService;
import com.mays.GreatIdeas.Service.UserService;

@Controller
public class UserController {
	private final UserService userService;
	private final IdeaService ideaService;
	private final UserRepository userRepository;
	private final IdeaRepository ideaRepository;

	public UserController(UserService userService, IdeaService ideaService, UserRepository userRepository,
			IdeaRepository ideaRepository) {
		this.userService = userService;
		this.ideaService = ideaService;
		this.userRepository = userRepository;
		this.ideaRepository = ideaRepository;
	}

	@GetMapping("/")
	public String home(@ModelAttribute("user") User user, @ModelAttribute("user_log") User user_log) {
		return "loginRegistration.jsp";
	}


	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, HttpSession session) {
		if (result.hasErrors()) {
			return "loginRegistration.jsp";
		} else {
			User u = userService.registerUser(user);
			session.setAttribute("userId", u.getId());
			return "redirect:/ideas";
		}

	}


	@PostMapping(value = "/login")
	public String login(@ModelAttribute("user") User user, BindingResult result,
			@RequestParam("email_log") String email, @RequestParam("password_log") String password, HttpSession session,
			Model model) {
		if (userService.authenticateUser(email, password) == true) {
			User u = userService.findByEmail(email);
			session.setAttribute("userId", u.getId());
			return "redirect:/ideas";
		} else {
			model.addAttribute("error", "Invalid Credentials. Please try again.");
			return "loginRegistration.jsp";

		}
	}

	@RequestMapping("/ideas")
	public String home(Model model, HttpSession session) {
		List<Idea> ideas = ideaService.findAllIdeas();
		model.addAttribute("ideas", ideas);
		User u = userService.findUserById((Long) session.getAttribute("userId"));
		model.addAttribute("user", u);
		return "index.jsp";
	}

	@GetMapping("/ideas/new")
	public String createIdea(@ModelAttribute("idea") Idea idea) {
		return "newIdea.jsp";
	}

	@PostMapping("/ideas/new")
	public String createIdeaPost(@Valid @ModelAttribute("idea") Idea idea, BindingResult result, HttpSession session) {
		if (result.hasErrors()) {
			return "newIdea.jsp";
		} else {
			User u = userService.findUserById((Long) session.getAttribute("userId"));
			idea.setCreator(u);
			Idea m = ideaRepository.save(idea);
			return "redirect:/ideas";
		}
	}

	@GetMapping("/show/{id}")
	public String show(@PathVariable("id") Long id, Model model) {
		Idea m = ideaService.findIdeaById(id);
		model.addAttribute("idea", m);
		
		return "showIdea.jsp";
	}

	@GetMapping("/ideas/edit/{id}")
	public String edit(@PathVariable("id") Long id, Model model) {
		Idea m = ideaService.findIdeaById(id);
		model.addAttribute("idea", m);
		return "editIdea.jsp";
	}

	@PostMapping("/ideas/edit/{id}")
	public String actuallyEdit(@PathVariable("id") Long id, @Valid @ModelAttribute("idea") Idea idea,
			BindingResult result) {
		if (result.hasErrors()) {
			return "editIdea.jsp";
		} else {
			Idea old = ideaService.findIdeaById(id);
			idea.setCreator(old.getCreator());
			idea.setLikers(old.getLikers());
			ideaRepository.save(idea);
			return "redirect:/ideas";
		}
	}

	@GetMapping("/like/{id}")
	public String like(@PathVariable("id") Long id, HttpSession session) {
		User u = userService.findUserById((Long) session.getAttribute("userId"));
		Idea m = ideaService.findIdeaById(id);
		m.getLikers().add(u);
		ideaRepository.save(m);
		return "redirect:/ideas";
	}

	@GetMapping("/unlike/{id}")
	public String unlike(@PathVariable("id") Long id, HttpSession session) {
		User u = userService.findUserById((Long) session.getAttribute("userId"));
		Idea m = ideaService.findIdeaById(id);
		for (int i = 0; i < m.getLikers().size(); i++) {
			if (u.getId() == m.getLikers().get(i).getId()) {
				m.getLikers().remove(i);
				break;
			}
		}
		ideaRepository.save(m);
		return "redirect:/ideas";
	}

	@GetMapping("/delete/{id}")
	public String delete(@PathVariable("id") Long id) {
		ideaRepository.deleteById(id);
		return "redirect:/ideas";
	}

	@RequestMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/";
	}
}